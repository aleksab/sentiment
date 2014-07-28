package no.hioa.sentiment.sopmi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateSoPmi
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		PropertyConfigurator.configure("log4j.properties");

		new GenerateSoPmi().generateSoPmiLists("pmi-news.d100.csv");
		new GenerateSoPmi().generateSoPmiLists("pmi-adjectives.d100.csv");
		new GenerateSoPmi().generateSoPmiLists("pmi-forum.d100.csv");

		new GenerateSoPmi().generateOccurenceSoPmiLists("pmi-forum.d100.csv", "wordBlockOccurence-forum.d100.csv", 100);
		new GenerateSoPmi().generateOccurenceSoPmiLists("pmi-news.d100.csv", "wordBlockOccurence-news.d100.csv", 100);
		new GenerateSoPmi().generateOccurenceSoPmiLists("pmi-adjectives.d100.csv", "wordBlockOccurence-adjectives.d100.csv", 100);

		new GenerateSoPmi().generateOccurenceSoPmiLists("pmi-forum.d100.csv", "wordBlockOccurence-forum.d100.csv", 1000);
		new GenerateSoPmi().generateOccurenceSoPmiLists("pmi-news.d100.csv", "wordBlockOccurence-news.d100.csv", 1000);
		new GenerateSoPmi().generateOccurenceSoPmiLists("pmi-adjectives.d100.csv", "wordBlockOccurence-adjectives.d100.csv", 1000);

		new GenerateSoPmi().generateCombinedSoPmiLists("pmi-combined-news.d100.csv", "pmi-news.d100.csv", "pmi-adjectives.d100.csv");
		new GenerateSoPmi().generateCombinedSoPmiLists("pmi-combined-all.d100.csv", "pmi-news.d100.csv", "pmi-adjectives.d100.csv",
				"pmi-forum.d100.csv");
	}

	public void generateSoPmiLists(String pmiFile)
	{
		HashMap<String, BigDecimal> soPmi = getSoPmi(pmiFile);
		HashMap<String, BigDecimal> normalizeSoPmi = normalizeSoPmi(soPmi);
		HashMap<String, BigDecimal> normalize2SoPmi = normalizeHighSoPmi(soPmi);

		writeFile("so-" + pmiFile, soPmi);
		writeFile("normalized.so-" + pmiFile, normalizeSoPmi);
		writeFile("normalized-high.so-" + pmiFile, normalize2SoPmi);

		consoleLogger.info("Generated regular SO-PMI lists for " + pmiFile);
	}

	public void generateOccurenceSoPmiLists(String pmiFile, String wordOccurenceFile, int minOccurence)
	{
		HashMap<String, HashMap<String, BigDecimal>> wordOccurence = getWordOccurence(wordOccurenceFile);
		HashMap<String, BigDecimal> soPmi = getSoPmi(pmiFile, wordOccurence, minOccurence);
		HashMap<String, BigDecimal> normalizeSoPmi = normalizeSoPmi(soPmi);
		HashMap<String, BigDecimal> normalize2SoPmi = normalizeHighSoPmi(soPmi);

		writeFile("occurence-min" + minOccurence + ".so-" + pmiFile, soPmi);
		writeFile("occurence-min" + minOccurence + "-normalized.so-" + pmiFile, normalizeSoPmi);
		writeFile("occurence-min" + minOccurence + "-normalized-high.so-" + pmiFile, normalize2SoPmi);

		consoleLogger.info("Generated occurence " + minOccurence + " SO-PMI lists for " + pmiFile);
	}

	public void generateCombinedSoPmiLists(String postFix, String... pmiFiles)
	{

		HashMap<String, BigDecimal> soPmi = getCombinedSoPmi(pmiFiles);
		HashMap<String, BigDecimal> normalizeSoPmi = normalizeSoPmi(soPmi);
		HashMap<String, BigDecimal> normalize2SoPmi = normalizeHighSoPmi(soPmi);

		writeFile("so-" + postFix, soPmi);
		writeFile("normalized.so-" + postFix, normalizeSoPmi);
		writeFile("normalized-high.so-" + postFix, normalize2SoPmi);

		consoleLogger.info("Generated combined SO-PMI lists");
	}

	HashMap<String, BigDecimal> normalizeSoPmi(HashMap<String, BigDecimal> soPmi)
	{
		HashMap<String, BigDecimal> normalizedSoPmi = new HashMap<>();

		for (String word : soPmi.keySet())
		{
			BigDecimal score = soPmi.get(word);
			BigDecimal normalizedScore = BigDecimal.ZERO.setScale(5);

			if (score.compareTo(BigDecimal.ZERO) > 0)
				normalizedScore = BigDecimal.ONE;
			else if (score.compareTo(BigDecimal.ZERO) < 0)
				normalizedScore = new BigDecimal("-1");
			else
				normalizedScore = BigDecimal.ZERO;

			normalizedSoPmi.put(word, normalizedScore);
		}

		return normalizedSoPmi;
	}

	HashMap<String, BigDecimal> normalizeHighSoPmi(HashMap<String, BigDecimal> soPmi)
	{
		HashMap<String, BigDecimal> normalizedSoPmi = new HashMap<>();

		for (String word : soPmi.keySet())
		{
			BigDecimal score = soPmi.get(word);
			BigDecimal normalizedScore = BigDecimal.ZERO.setScale(5);

			if (score.compareTo(new BigDecimal("1")) >= 0)
				normalizedScore = BigDecimal.ONE;
			else if (score.compareTo(new BigDecimal("-1")) <= 0)
				normalizedScore = new BigDecimal("-1");
			else
				normalizedScore = BigDecimal.ZERO;

			normalizedSoPmi.put(word, normalizedScore);
		}

		return normalizedSoPmi;
	}

	public HashMap<String, BigDecimal> getCombinedSoPmi(String... pmiFiles)
	{
		HashMap<String, BigDecimal> soPmiCombined = new HashMap<>();
		int duplicate = 0;
		int replaced = 0;

		for (String pmiFile : pmiFiles)
		{
			HashMap<String, BigDecimal> soPmi = getSoPmi(pmiFile);

			for (String word : soPmi.keySet())
			{
				if (soPmiCombined.containsKey(word))
				{
					duplicate++;

					BigDecimal newValue = soPmi.get(word);
					BigDecimal exValue = soPmiCombined.get(word);

					if (newValue.compareTo(exValue) != 0)
					{
						BigDecimal avgValue = newValue.add(exValue).divide(new BigDecimal(2), RoundingMode.CEILING);
						soPmiCombined.put(word, avgValue);
						replaced++;
					}
				}
				else
				{
					soPmiCombined.put(word, soPmi.get(word));
				}
			}
		}

		consoleLogger.info("Found {} duplicates and {} were replaced", duplicate, replaced);

		return soPmiCombined;
	}

	HashMap<String, BigDecimal> getSoPmi(String pmiFile)
	{
		HashMap<String, BigDecimal> soPmi = new HashMap<>();

		try (Scanner scanner = new Scanner(new FileInputStream("src/main/resources/no/hioa/sentiment/sopmi/" + pmiFile), "ISO-8859-1"))
		{
			// skip first 4 lines
			for (int i = 0; i < 4; i++)
				scanner.nextLine();

			while (scanner.hasNextLine())
			{
				String[] input = scanner.nextLine().toLowerCase().split(",");
				String word = input[0];
				BigDecimal score = BigDecimal.ZERO.setScale(5);

				for (int i = 1; i <= 7; i++)
					score = score.add(new BigDecimal(input[i]));
				for (int i = 8; i <= 14; i++)
					score = score.subtract(new BigDecimal(input[i]));

				soPmi.put(word, score);
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not read pmi from file " + pmiFile, ex);
		}

		return soPmi;
	}

	HashMap<String, BigDecimal> getSoPmi(String pmiFile, HashMap<String, HashMap<String, BigDecimal>> wordOccurences, int minOccurence)
	{
		HashMap<String, BigDecimal> soPmi = new HashMap<>();

		try (Scanner scanner = new Scanner(new FileInputStream("src/main/resources/no/hioa/sentiment/sopmi/" + pmiFile), "ISO-8859-1"))
		{
			// skip first 3 lines
			for (int i = 0; i < 3; i++)
				scanner.nextLine();

			// get all the seed words
			String[] words = scanner.nextLine().split(",");

			if (words.length != 16)
				throw new RuntimeException("Not 16 words in " + pmiFile);

			while (scanner.hasNextLine())
			{
				String[] input = scanner.nextLine().toLowerCase().split(",");
				String word = input[0];
				BigDecimal score = BigDecimal.ZERO.setScale(5);

				HashMap<String, BigDecimal> wordOccurence = wordOccurences.get(word);

				for (int i = 1; i <= 7; i++)
				{
					String seedWord = words[i];
					BigDecimal occurence = wordOccurence.get(seedWord);

					if (occurence == null)
					{
						throw new RuntimeException("Could not find occurence for word " + seedWord);
					}
					else if (occurence.intValue() >= minOccurence)
						score = score.add(new BigDecimal(input[i]));
				}

				for (int i = 8; i <= 14; i++)
				{
					String seedWord = words[i];
					int occurence = wordOccurence.get(seedWord).intValue();

					if (occurence >= minOccurence)
						score = score.subtract(new BigDecimal(input[i]));
				}

				soPmi.put(word, score);
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not read pmi from file " + pmiFile, ex);
		}

		return soPmi;
	}

	HashMap<String, HashMap<String, BigDecimal>> getWordOccurence(String wordOccurenceFile)
	{
		HashMap<String, HashMap<String, BigDecimal>> wordOccurence = new HashMap<>();

		try (Scanner scanner = new Scanner(new FileInputStream("src/main/resources/no/hioa/sentiment/sopmi/" + wordOccurenceFile), "ISO-8859-1"))
		{
			// skip first 3 lines
			for (int i = 0; i < 3; i++)
				scanner.nextLine();

			// get all the seed words
			String[] words = scanner.nextLine().split(",");

			if (words.length != 16)
				throw new RuntimeException("Not 16 words in " + wordOccurenceFile);

			while (scanner.hasNextLine())
			{
				String[] input = scanner.nextLine().toLowerCase().split(",");
				String word = input[0];
				HashMap<String, BigDecimal> occurences = new HashMap<>();

				for (int i = 1; i <= 14; i++)
				{
					BigDecimal occurence = new BigDecimal(input[i]);
					occurences.put(words[i], occurence);
				}

				wordOccurence.put(word, occurences);
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not read occurence from file " + wordOccurenceFile, ex);
		}

		return wordOccurence;
	}

	void writeFile(String file, HashMap<String, BigDecimal> soPmi)
	{
		try
		{
			PrintWriter writter = new PrintWriter("target/" + file, "UTF-8");
			for (String word : soPmi.keySet())
			{
				writter.append(word + "\t" + soPmi.get(word) + "\n");
			}
			writter.close();
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not write to file " + file, ex);
		}
	}
}
