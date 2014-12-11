package no.hioa.sentiment.score;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class CalcSentimentAgreement
{
	private static final Logger	logger					= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-folder", description = "Folder with sentiment scores", required = true)
	private String				sentimentScoreFolder	= "";

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		new CalcSentimentAgreement(args);
	}

	public CalcSentimentAgreement(String[] args)
	{
		new JCommander(this, args);

		calculateAverageSentimentScore();
	}

	public void calculateAverageSentimentScore()
	{
		HashMap<String, TotalWordScore> allWords = new HashMap<>();

		try
		{
			File[] files = new File(sentimentScoreFolder).listFiles();
			for (File file : files)
			{
				logger.info("Reading sentiment scores from {}", file.getName());
				List<SentimentWord> words = getSentimentWords(file);

				for (SentimentWord word : words)
				{
					TotalWordScore score = null;
					if (allWords.containsKey(word.getWord()))
					{
						score = allWords.get(word.getWord());
					}
					else
					{
						score = new TotalWordScore();
						allWords.put(word.getWord(), score);
					}

					score.addScore(word.getRating());
				}
			}

			logger.info("Calculating average score for all {} words", allWords.keySet().size());
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

	private class TotalWordScore
	{
		private List<BigDecimal>	scores	= new LinkedList<>();

		public void addScore(BigDecimal score)
		{
			scores.add(score);
		}

		public List<BigDecimal> getScores()
		{
			return scores;
		}
	}
}
