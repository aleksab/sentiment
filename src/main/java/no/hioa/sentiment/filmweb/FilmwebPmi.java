package no.hioa.sentiment.filmweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import no.hioa.sentiment.service.PmiCalculator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;

public class FilmwebPmi implements PmiCalculator
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	MongoOperations mongoOperations;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new FilmwebPmi().calculateCandidatePmi(new File("target/"), "so-pmi-10.txt", 10);
	}

	public FilmwebPmi() throws UnknownHostException
	{
		mongoOperations = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), "filmweb"));
	}

	public void calculateCandidatePmi(File outputDir, String filname, int limit)
	{
		List<String> candidates = getCandidateWords();
		List<String> pWords = getPositiveWords();
		List<String> nWords = getNegativeWords();
		Map<String, BigDecimal> soPmi = new HashMap<>();

		for (String candidate : candidates)
		{
			consoleLogger.info("Calculating SO-PMI for candidate word {} with limit {}", candidate, limit);
			soPmi.put(candidate, calculateSoPmi(candidate, pWords, nWords, limit));
		}

		soPmi = MapUtil.sortByValue(soPmi);

		Path newFile = Paths.get(outputDir.getAbsolutePath(), filname);
		consoleLogger.info("Saving result to file " + newFile);
		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			for (String word : soPmi.keySet())
			{
				writer.append("Word " + word + " has SO-PMI of " + soPmi.get(word) + "\n");
				consoleLogger.info("Word {} has SO-PMI of {}", word, soPmi.get(word));
			}
		} catch (IOException ex)
		{
			logger.error("Could not save SO-PMI to file " + newFile, ex);
		}
	}

	public BigDecimal calculateSoPmi(String word, List<String> pWords, List<String> nWords, int limit)
	{
		BigDecimal totalPositive = BigDecimal.ZERO.setScale(5);
		BigDecimal totalNegative = BigDecimal.ZERO.setScale(5);

		for (String positive : pWords)
		{
			totalPositive = totalPositive.add(calculatePmi(word, positive, limit));
		}

		for (String negative : nWords)
		{
			totalNegative = totalNegative.add(calculatePmi(word, negative, limit));
		}

		logger.info("Total positive for word {} is {}", word, totalPositive);
		logger.info("Total negative for word {} is {}", word, totalNegative);

		BigDecimal soPmi = totalPositive.subtract(totalNegative).setScale(5, RoundingMode.CEILING);
		consoleLogger.info("Total SO-PMI for word {} is {} with limit {}", word, soPmi, limit);

		return soPmi;
	}

	@Override
	public BigDecimal calculatePmi(String word1, String word2, int limit)
	{
		List<Review> reviews = mongoOperations.findAll(Review.class);
		logger.info("There are {} reviews to calculate pmi for word1 {} and word2 {} with limit {}", reviews.size(), word1, word2, limit);

		BigDecimal totalDocuments = BigDecimal.ZERO.setScale(5);
		BigDecimal occurenceWord1 = BigDecimal.ZERO.setScale(5);
		BigDecimal occurenceWord2 = BigDecimal.ZERO.setScale(5);
		BigDecimal occurenceNear = BigDecimal.ZERO.setScale(5);

		for (Review review : reviews)
		{
			totalDocuments = totalDocuments.add(BigDecimal.ONE);

			if (StringUtils.contains(review.getContent(), word1))
				occurenceWord1 = occurenceWord1.add(BigDecimal.ONE);
			if (StringUtils.contains(review.getContent(), word2))
				occurenceWord2 = occurenceWord2.add(BigDecimal.ONE);
			if (isWithinLimit(review.getContent(), word1, word2, limit))
				occurenceNear = occurenceNear.add(BigDecimal.ONE);
		}

		logger.info("Total documents {}", totalDocuments);
		logger.info("Occurence of word1 ({}) {}", word1, occurenceWord1);
		logger.info("Occurence of word2 ({}) {}", word2, occurenceWord2);
		logger.info("Occurence of both words with limit ({}) {}", limit, occurenceNear);

		BigDecimal probabilityWord1 = occurenceWord1.divide(totalDocuments, RoundingMode.CEILING);
		BigDecimal probabilityWord2 = occurenceWord2.divide(totalDocuments, RoundingMode.CEILING);
		BigDecimal probabilityBoth = occurenceNear.divide(totalDocuments, RoundingMode.CEILING);

		logger.info("Probability of word1 ({}) {}", word1, probabilityWord1);
		logger.info("Probability of word2 ({}) {}", word2, probabilityWord2);
		logger.info("Probability of both words {}", probabilityBoth);

		BigDecimal pmi = calculateBasePmi(probabilityBoth, probabilityWord1, probabilityWord2);
		logger.info("PMI for {} and {} within limit {} is {}", word1, word2, limit, pmi);

		return pmi;
	}

	BigDecimal calculateBasePmi(BigDecimal occurenceBoth, BigDecimal occurenceWord1, BigDecimal occurenceWord2)
	{
		occurenceBoth = occurenceBoth.setScale(5);
		occurenceWord1 = occurenceWord1.setScale(5);
		occurenceWord2 = occurenceWord2.setScale(5);

		// TODO: should we do this or use Laplace smoothing?
		if (occurenceBoth.compareTo(BigDecimal.ZERO) == 0 || occurenceWord1.multiply(occurenceWord2).compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		BigDecimal result = occurenceBoth.divide(occurenceWord1.multiply(occurenceWord2), RoundingMode.CEILING).setScale(5);

		// TODO: this is not ideal and we might lose precision
		return new BigDecimal(Math.log(result.floatValue()) / Math.log(2));
	}

	/**
	 * Check if two words are within a certain range of each other in a content.
	 * 
	 * @param content
	 * @param word1
	 * @param word2
	 * @param limit
	 * @return
	 */
	boolean isWithinLimit(String content, String word1, String word2, int limit)
	{
		if (!content.contains(word1) || !content.contains(word2))
			return false;

		if (limit <= 0 || limit > content.length())
			return true;

		StringTokenizer words = new StringTokenizer(content);
		int word1position = -1;
		int word2position = -1;
		int counter = 0;

		while (words.hasMoreTokens())
		{
			counter++;
			String word = words.nextToken();

			if (word.contains(word1))
				word1position = counter;

			if (word.contains(word2))
				word2position = counter;

			if (word1position != -1 && word2position != -1)
			{
				if (Math.abs(word1position - word2position) <= limit)
					return true;
			}
		}

		return false;
	}

	List<String> getCandidateWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/filmweb/candidate.txt"));
	}

	List<String> getPositiveWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/filmweb/positive.txt"));
	}

	List<String> getNegativeWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/filmweb/negative.txt"));
	}

	private List<String> getFileContent(File file)
	{
		List<String> words = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				words.add(input);
			}
		} catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return words;
	}
}
