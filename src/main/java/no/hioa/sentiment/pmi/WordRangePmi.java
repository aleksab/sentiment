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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class WordRangePmi
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("fileLogger");

	@Parameter(names = "-db", description = "Mongo database to use", required = true)
	private String				dbName;

	@Parameter(names = "-start", description = "Start distance between words")
	private int					startDistance	= 1;

	@Parameter(names = "-end", description = "End distance between words")
	private int					endDistance		= 1000;

	@Parameter(names = "-word", description = "Word to check range for", required = true)
	private String				word;

	private PmiCalculator		pmiCalculator;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new WordRangePmi(args);
	}

	public WordRangePmi(String[] args) throws UnknownHostException
	{
		new JCommander(this, args);
		pmiCalculator = new DefaultPmiCalculator(Corpus.MOVIE_REVIEWS);

		// calculateWordRangePmi(new File("target/"), startDistance, endDistance, word);
		calculateCandidateRangePmi(new File("target/"), startDistance, endDistance);
	}

	private void calculateCandidateRangePmi(File outputDir, int startDistance, int endDistance)
	{
		List<String> candidates = SeedProvider.getCandidateWords();		
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();

		String fileName = "so-pmi-range-" + startDistance + "-" + endDistance + "-all.txt";
		Path newFile = Paths.get(outputDir.getAbsolutePath(), fileName);
		consoleLogger.info("Saving result to file " + newFile);

		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			// write header
			writer.append("SO-PMI range for all candidate words\n");
			String buffer = "Range,";
			int limit = startDistance;
			while (limit <= endDistance)
			{
				buffer += limit + ",";

				if (limit < 10)
					limit++;
				else if (limit < 100)
					limit += 10;
				else if (limit < 1000)
					limit += 50;
				else
					break;
			}

			buffer = StringUtils.substringBeforeLast(buffer, ",");
			writer.append(buffer + "\n");

			for (String candidate : candidates)
			{
				Map<Integer, BigDecimal> soPmi = new HashMap<>();

				limit = startDistance;
				while (limit <= endDistance)
				{
					consoleLogger.info("Calculating SO-PMI for word {} with limit {}", candidate, limit);
					soPmi.put(limit, pmiCalculator.calculateSoPmi(candidate, pWords, nWords, limit));

					if (limit < 10)
						limit++;
					else if (limit < 100)
						limit += 10;
					else if (limit < 1000)
						limit += 50;
					else
						break;
				}

				soPmi = MapUtil.sortByKey(soPmi);

				buffer = candidate + ",";
				for (Integer distance : soPmi.keySet())
				{
					buffer += soPmi.get(distance) + ",";
				}

				buffer = StringUtils.substringBeforeLast(buffer, ",");
				writer.append(buffer + "\n");

				consoleLogger.info("Word {} has SO-PMI range", candidate, buffer);
			}
		}
		catch (IOException ex)
		{
			consoleLogger.error("Could not save SO-PMI to file " + newFile, ex);
		}
	}

	/**
	 * Calculate SO-PMI for a range of distances for a given word.
	 * 
	 * @param outputDir
	 * @param startDistance
	 * @param endDistance
	 * @param word
	 */
	private void calculateWordRangePmi(File outputDir, int startDistance, int endDistance, String word)
	{
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();
		Map<Integer, BigDecimal> soPmi = new HashMap<>();

		int limit = startDistance;
		while (limit <= endDistance)
		{
			consoleLogger.info("Calculating SO-PMI for word {} with limit {}", word, limit);
			soPmi.put(limit, pmiCalculator.calculateSoPmi(word, pWords, nWords, limit));

			if (limit < 10)
				limit++;
			else if (limit < 100)
				limit += 10;
			else if (limit < 1000)
				limit += 50;
			else
				break;
		}

		soPmi = MapUtil.sortByKey(soPmi);

		String fileName = "so-pmi-range-" + startDistance + "-" + endDistance + "-" + word + ".txt";
		Path newFile = Paths.get(outputDir.getAbsolutePath(), fileName);
		consoleLogger.info("Saving result to file " + newFile);
		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			writer.append("SO-PMI range for " + word + "\n");
			for (Integer distance : soPmi.keySet())
			{
				writer.append(distance + "," + soPmi.get(distance) + "\n");
				consoleLogger.info("Word {} has SO-PMI of {} for distance {}", word, soPmi.get(distance), distance);
			}
		}
		catch (IOException ex)
		{
			consoleLogger.error("Could not save SO-PMI to file " + newFile, ex);
		}
	}
}
