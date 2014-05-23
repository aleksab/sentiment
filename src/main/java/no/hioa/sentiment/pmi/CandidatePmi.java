package no.hioa.sentiment.pmi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.SeedProvider;
import no.hioa.sentiment.util.MapUtil;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class CandidatePmi
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("fileLogger");

	@Parameter(names = "-db", description = "Mongo database to use", required = true)
	private String				dbName;

	@Parameter(names = "-max", description = "Max distance between words")
	private int					maxDistance		= 100;

	private PmiCalculator		pmiCalculator;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new CandidatePmi(args);
	}

	public CandidatePmi(String[] args) throws UnknownHostException
	{
		new JCommander(this, args);
		pmiCalculator = new DefaultPmiCalculator(Corpus.NEWSPAPER_ARTICLES);

		calculateCandidatePmi(new File("target/"), maxDistance);
	}

	/**
	 * Calculate SO-PMI for all candidate words.
	 * 
	 * @param outputDir
	 * @param filname
	 * @param maxDistance
	 */
	private void calculateCandidatePmi(File outputDir, int maxDistance)
	{
		List<String> candidates = SeedProvider.getCandidateWords();
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();
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
		}
		catch (IOException ex)
		{
			consoleLogger.error("Could not save SO-PMI to file " + newFile, ex);
		}
	}
}
