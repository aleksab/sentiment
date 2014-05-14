package no.hioa.sentiment.filmweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
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

import no.hioa.sentiment.pmi.DefaultPmiCalculator;
import no.hioa.sentiment.pmi.PmiCalculator;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;

public class FilmwebPmi
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	private MongoOperations mongoOperations;
	private PmiCalculator pmiCalculator;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new FilmwebPmi().calculateCandidatePmi(new File("target/"), 10);
	}

	public FilmwebPmi() throws UnknownHostException
	{
		mongoOperations = MongoProvider.getMongoProvider("filmweb");
		pmiCalculator = new DefaultPmiCalculator(mongoOperations);
	}

	/**
	 * Calculate SO-PMI for all candidate words.
	 * 
	 * @param outputDir
	 * @param filname
	 * @param maxDistance
	 */
	public void calculateCandidatePmi(File outputDir, int maxDistance)
	{
		List<String> candidates = getCandidateWords();
		List<String> pWords = getPositiveWords();
		List<String> nWords = getNegativeWords();
		Map<String, BigDecimal> soPmi = new HashMap<>();

		for (String candidate : candidates)
		{
			consoleLogger.info("Calculating SO-PMI for candidate word {} with limit {}", candidate, maxDistance);
			soPmi.put(candidate, pmiCalculator.calculateSoPmi(candidate, pWords, nWords, maxDistance));
		}

		soPmi = MapUtil.sortByValue(soPmi);

		String fileName = "so-pmi-" + maxDistance + ".txt";
		Path newFile = Paths.get(outputDir.getAbsolutePath(), fileName);
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
