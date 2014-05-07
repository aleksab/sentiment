package no.hioa.sentiment.filmweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import no.hioa.sentiment.service.PmiCalculator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class FilmwebPmi implements PmiCalculator
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Autowired
	MongoOperations				mongoOperations;

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");

		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/bootstrap.xml");
		FilmwebPmi pmi = context.getBean(FilmwebPmi.class);
		// pmi.calculatePmi("bra", "super");
		pmi.calculateCandidatePmi(new File("target/"));
	}

	public void calculateCandidatePmi(File outputDir)
	{
		List<String> candidates = getCandidateWords();
		List<String> pWords = getPositiveWords();
		List<String> nWords = getNegativeWords();
		Map<String, BigDecimal> soPmi = new HashMap<>();

		for (String candidate : candidates)
		{
			consoleLogger.info("Calculating SO-PMI for candidate word {}", candidate);
			soPmi.put(candidate, calculateSoPmi(candidate, pWords, nWords));
		}

		soPmi = MapUtil.sortByValue(soPmi);

		Path newFile = Paths.get(outputDir.getAbsolutePath(), "so-pmi.txt");
		consoleLogger.info("Saving result to file " + newFile);
		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			for (String word : soPmi.keySet())
			{
				writer.append("Word " + word + " has SO-PMI of " + soPmi.get(word) + "\n");
				consoleLogger.info("Word {} has SO-PMI of {}", word, soPmi.get(word));
			}
		}
		catch (IOException ex)
		{
			logger.error("Could not save SO-PMI to file " + newFile, ex);
		}
	}

	@Override
	public BigDecimal calculatePmi(String word1, String word2)
	{
		BigDecimal probabilityWord1 = BigDecimal.ZERO.setScale(5);
		BigDecimal probabilityWord2 = BigDecimal.ZERO.setScale(5);
		BigDecimal probabilityBoth = BigDecimal.ZERO.setScale(5);

		List<Movie> movies = mongoOperations.findAll(Movie.class);
		consoleLogger.info("There are {} movies to calculate pmi for word1 {} and word2 {}", movies.size(), word1, word2);

		BigDecimal totalDocuments = BigDecimal.ZERO.setScale(5);
		BigDecimal occurenceWord1 = BigDecimal.ZERO.setScale(5);
		BigDecimal occurenceWord2 = BigDecimal.ZERO.setScale(5);
		BigDecimal occurenceBoth = BigDecimal.ZERO.setScale(5);
		for (Movie movie : movies)
		{
			for (ReviewContent review : movie.getReviews())
			{
				totalDocuments = totalDocuments.add(BigDecimal.ONE);

				if (StringUtils.contains(review.getContent(), word1))
					occurenceWord1 = occurenceWord1.add(BigDecimal.ONE);
				if (StringUtils.contains(review.getContent(), word2))
					occurenceWord2 = occurenceWord2.add(BigDecimal.ONE);

				if (StringUtils.contains(review.getContent(), word1) && StringUtils.contains(review.getContent(), word2))
					occurenceBoth = occurenceBoth.add(BigDecimal.ONE);
			}
		}

		consoleLogger.info("Total documents {}", totalDocuments);
		consoleLogger.info("Occurence of word1 ({}) {}", word1, occurenceWord1);
		consoleLogger.info("Occurence of word2 ({}) {}", word2, occurenceWord2);
		consoleLogger.info("Occurence of both words {}", occurenceBoth);

		probabilityWord1 = occurenceWord1.divide(totalDocuments, RoundingMode.CEILING);
		probabilityWord2 = occurenceWord2.divide(totalDocuments, RoundingMode.CEILING);
		probabilityBoth = occurenceBoth.divide(totalDocuments, RoundingMode.CEILING);

		consoleLogger.info("Probability of word1 ({}) {}", word1, probabilityWord1);
		consoleLogger.info("Probability of word2 ({}) {}", word2, probabilityWord2);
		consoleLogger.info("Probability of both words {}", probabilityBoth);

		BigDecimal pmi = calculateBasePmi(probabilityBoth, probabilityWord1, probabilityWord2);
		consoleLogger.info("PMI for {} and {} is {}", word1, word2, pmi);

		return pmi;
	}

	@Override
	public BigDecimal calculateSoPmi(String word, List<String> pWords, List<String> nWords)
	{
		BigDecimal totalPositive = BigDecimal.ZERO.setScale(5);
		BigDecimal totalNegative = BigDecimal.ZERO.setScale(5);

		for (String positive : pWords)
		{
			totalPositive = totalPositive.add(calculatePmi(word, positive));
		}

		for (String negative : nWords)
		{
			totalNegative = totalNegative.add(calculatePmi(word, negative));
		}

		consoleLogger.info("Total positive for word {} is {}", word, totalPositive);
		consoleLogger.info("Total negative for word {} is {}", word, totalNegative);

		BigDecimal soPmi = totalPositive.subtract(totalNegative).setScale(5, RoundingMode.CEILING);
		consoleLogger.info("Total SO-PMI for word {} is {}", word, soPmi);

		return soPmi;
	}

	BigDecimal calculateBasePmi(BigDecimal occurenceBoth, BigDecimal occurenceWord1, BigDecimal occurenceWord2)
	{
		occurenceBoth = occurenceBoth.setScale(5);
		occurenceWord1 = occurenceWord1.setScale(5);
		occurenceWord2 = occurenceWord2.setScale(5);

		// if (occurenceBoth.compareTo(BigDecimal.ZERO) == 0)
		// occurenceBoth = new BigDecimal("0.01");
		// if (occurenceWord1.compareTo(BigDecimal.ZERO) == 0)
		// occurenceWord1 = new BigDecimal("0.01");
		// if (occurenceWord2.compareTo(BigDecimal.ZERO) == 0)
		// occurenceWord2 = new BigDecimal("0.01");

		// TODO: should we do this or use Laplace smoothing?
		if (occurenceBoth.compareTo(BigDecimal.ZERO) == 0 || occurenceWord1.multiply(occurenceWord2).compareTo(BigDecimal.ZERO) == 0)
			return BigDecimal.ZERO;

		BigDecimal result = occurenceBoth.divide(occurenceWord1.multiply(occurenceWord2), RoundingMode.CEILING).setScale(5);

		// TODO: this is not ideal and we might lose precision
		return new BigDecimal(Math.log(result.floatValue()) / Math.log(2));
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
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return words;
	}
}
