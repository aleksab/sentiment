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
	private static final Logger	logger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-file", description = "File with sentiment score", required = false)
	private String				file	= null;

	@Parameter(names = "-folder", description = "File with sentiment score", required = false)
	private String				folder	= null;

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		new TrustAlgoReview(args);
	}

	public TrustAlgoReview(String[] args)
	{
		new JCommander(this, args);

		if (folder != null)
		{
			for (File file : new File(folder).listFiles())
			{
				rateReviews(ReviewType.FILMWEB, file.getAbsolutePath());
				rateReviews(ReviewType.KOMPLETT, file.getAbsolutePath());
				rateReviews(ReviewType.MPX, file.getAbsolutePath());
			}
		}
		else
		{
			rateReviews(ReviewType.FILMWEB, file);
			rateReviews(ReviewType.KOMPLETT, file);
			rateReviews(ReviewType.MPX, file);
		}
	}

	public void rateReviews(ReviewType type, String file)
	{
		List<SentimentWord> sentimentWords = getSentimentWords(new File(file));
		List<String> shifters = new LinkedList<>();
		shifters.add("ikke");

		try
		{
			logger.info("Calculating score for review {}", type);
			DefaultSentimentScore scorer = new DefaultSentimentScore(Corpus.REVIEWS);
			List<Score> scores = scorer.getSentimentScore(type, sentimentWords, shifters, true);
			logger.info("Calculated {} scores", scores.size());

			Path newFile = Paths.get("target/", new File(file).getName() + "-" + type.getName() + ".score");

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
