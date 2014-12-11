package no.hioa.sentiment.score;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import no.hioa.sentiment.review.ReviewType;
import no.hioa.sentiment.service.Corpus;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class TrustAlgoReview
{
	private static final Logger	logger			= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-file", description = "File with sentiment score", required = true)
	private String				sentimentScore	= "";

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		new TrustAlgoReview(args);
	}

	public TrustAlgoReview(String[] args)
	{
		new JCommander(this, args);

		rateReviews(ReviewType.FILMWEB);
		rateReviews(ReviewType.KOMPLETT);
		rateReviews(ReviewType.MPX);
	}

	public void rateReviews(ReviewType type)
	{
		List<SentimentWord> sentimentWords = getSentimentWords(new File(sentimentScore));
		List<String> shifters = new LinkedList<>();
		shifters.add("ikke");

		try
		{
			logger.info("Calculating score for review {}", type);
			DefaultSentimentScore scorer = new DefaultSentimentScore(Corpus.REVIEWS);
			List<Score> scores = scorer.getSentimentScore(type, sentimentWords, shifters, true);
			logger.info("Calculated {} scores", scores.size());

			Path newFile = Paths.get("target/", new File(sentimentScore).getName() + "-" + type.getName() + ".score");

			try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
			{
				for (Score score : scores)
				{
					writer.append(score.getReviewId() + "\t");
					writer.append(score.getSentimentScore() + "\t");
					writer.append(score.getReviewRating() + "\n");
				}
			}
			catch (IOException ex)
			{
				logger.error("Could not save score to file " + newFile, ex);
			}

			logger.info("Scores written to " + newFile);
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
		}
	}

	private List<SentimentWord> getSentimentWords(File file)
	{
		List<SentimentWord> words = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			while (scanner.hasNextLine())
			{
				String[] input = scanner.nextLine().toLowerCase().split("\t");
				String word = input[0].trim();
				BigDecimal rating = new BigDecimal(input[2].trim());
				words.add(new SentimentWord(word, rating));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return words;
	}
}
