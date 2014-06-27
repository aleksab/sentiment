package no.hioa.sentiment.newsletter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import no.hioa.sentiment.pmi.DefaultPmiCalculator.PmiResult;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.service.SeedProvider;
import no.hioa.sentiment.util.MapUtil;
import no.hioa.sentiment.util.WordUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class NewsletterData
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private ArticleRepository	repository;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		// new NewsletterData().extractMostCommonWords(new File("target/topwords.txt"), Collections.<String> emptyList(), -1);
		// new NewsletterData().extractMostCommonWords(new File("target/topwords.txt"), SeedProvider.getStopWords(), 10000);
		// new NewsletterData().countWords(new File("target/topwords.txt"));
		// new NewsletterData().countWords(new File("target/topwords.stripped.txt"));
		// new NewsletterData().removeStopWords(new File("target/topwords.txt"), new File("target/topwords.stripped.txt"),
		// SeedProvider.getStopWords());
		// new NewsletterData().findArticlesForWord("målemani");
		new NewsletterData().calculatePmiForAllWords(new File("target/topwords.stripped.txt"), 10000, 100);
	}

	public NewsletterData() throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider("s1", Corpus.NEWSPAPER_ARTICLES);

		RepositoryFactorySupport factory = new MongoRepositoryFactory(mongoOperations);
		this.repository = factory.getRepository(ArticleRepository.class);
	}

	public void calculatePmiForAllWords(File wordFile, long maxWords, int maxDistance) throws UnknownHostException, FileNotFoundException,
			UnsupportedEncodingException
	{
		List<String> targetWords = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(wordFile), "ISO-8859-1"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				targetWords.add(input.split(":")[0]);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		consoleLogger.info("Calculating pmi for {} words", maxWords);

		List<String> seedWords = SeedProvider.getPositiveWords();
		seedWords.addAll(SeedProvider.getNegativeWords());

		DefaultPmiCalculator pmi = new DefaultPmiCalculator(Corpus.NEWSPAPER_ARTICLES);

		PrintWriter pmiWritter = new PrintWriter("target/pmi.d" + maxDistance + ".csv", "ISO-8859-1");
		PrintWriter wordBlockOccurenceWritter = new PrintWriter("target/wordBlockOccurence.d" + maxDistance + ".csv", "ISO-8859-1");
		PrintWriter seedBlockOccurenceWritter = new PrintWriter("target/seedBlockOccurence.d" + maxDistance + ".csv", "ISO-8859-1");
		PrintWriter wordOccurenceWritter = new PrintWriter("target/wordOccurence.d" + maxDistance + ".csv", "ISO-8859-1");

		pmiWritter.append("Total words: " + pmi.getTotalWords() + "\nDistance: " + maxDistance + "\n\n");
		wordBlockOccurenceWritter.append("Total words: " + pmi.getTotalWords() + "\nDistance: " + maxDistance + "\n\n");
		seedBlockOccurenceWritter.append("Total words: " + pmi.getTotalWords() + "\nDistance: " + maxDistance + "\n\n");
		wordOccurenceWritter.append("Total words: " + pmi.getTotalWords() + "\nDistance: " + maxDistance + "\n\n");

		pmiWritter.append("TargetWord,");
		wordBlockOccurenceWritter.append("TargetWord,");
		seedBlockOccurenceWritter.append("TargetWord,");
		wordOccurenceWritter.append("TargetWord,");
		for (String seedWord : seedWords)
		{
			pmiWritter.append(StringUtils.capitalize(seedWord) + ",");
			wordBlockOccurenceWritter.append(StringUtils.capitalize(seedWord) + ",");
			seedBlockOccurenceWritter.append(StringUtils.capitalize(seedWord) + ",");
			wordOccurenceWritter.append(StringUtils.capitalize(seedWord) + ",");
		}
		pmiWritter.append("Completed\n");
		wordBlockOccurenceWritter.append("Completed\n");
		seedBlockOccurenceWritter.append("Completed\n");
		wordOccurenceWritter.append("Completed\n");

		int counter = 1;
		for (String targetWord : targetWords)
		{
			consoleLogger.info("Calculating pmi for word {} ({})", targetWord, counter);

			pmiWritter.append(targetWord + ",");
			wordBlockOccurenceWritter.append(targetWord + ",");
			seedBlockOccurenceWritter.append(targetWord + ",");
			wordOccurenceWritter.append(targetWord + ",");
			for (String seedWord : seedWords)
			{
				PmiResult result = pmi.calculatePmiForBlocks(targetWord, seedWord, maxDistance);
				pmiWritter.append(result.result.toPlainString() + ",");
				wordBlockOccurenceWritter.append(result.wordBlockOccurence.toPlainString() + ",");
				seedBlockOccurenceWritter.append(result.seedBlockOccurence.toPlainString() + ",");
				wordOccurenceWritter.append(result.wordOccurence.toPlainString() + ",");
			}
			pmiWritter.append("OK\n");
			wordBlockOccurenceWritter.append("OK\n");
			seedBlockOccurenceWritter.append("OK\n");
			wordOccurenceWritter.append("OK\n");

			pmiWritter.flush();
			wordBlockOccurenceWritter.flush();
			seedBlockOccurenceWritter.flush();
			wordOccurenceWritter.flush();
			
			if (counter++ >= maxWords)
			{
				logger.info("Limit of words to calulcate pmi for has been reached");
				break;
			}
		}

		pmiWritter.close();
		wordBlockOccurenceWritter.close();
		seedBlockOccurenceWritter.close();
		wordOccurenceWritter.close();
	}

	public void findArticlesForWord(String word) throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(Corpus.NEWSPAPER_ARTICLES);

		BasicQuery textQuery = new BasicQuery("{ $text: { $search: '" + word + "' } }");
		long articles = mongoOperations.count(textQuery, Article.class);

		logger.info("Found {} articles with word {}", articles, word);

		List<Article> articleList = mongoOperations.find(textQuery, Article.class);
		for (Article article : articleList)
		{
			logger.info(article.getContent());
		}
	}

	public void countWords(File allWords)
	{
		BigDecimal counter = BigDecimal.ZERO;
		try (Scanner scanner = new Scanner(new FileInputStream(allWords), "ISO-8859-1"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				int occurences = Integer.valueOf(input.split(":")[1]);
				counter = counter.add(new BigDecimal(occurences));
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + allWords.getAbsolutePath(), ex);
		}

		logger.info("Total number of words: {}", counter);
	}

	public void removeStopWords(File input, File output, List<String> stopWords)
	{
		Map<String, BigDecimal> occurences = new HashMap<>();

		try (Scanner scanner = new Scanner(new FileInputStream(input), "ISO-8859-1"))
		{
			while (scanner.hasNextLine())
			{
				String inputWord = scanner.nextLine().toLowerCase();
				String word = inputWord.split(":")[0];
				double wordOccurence = Double.valueOf(inputWord.split(":")[1]);

				// remove stopwords
				if (stopWords.contains(word))
					continue;

				occurences.put(word, new BigDecimal(wordOccurence));
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + input.getAbsolutePath(), ex);
		}

		logger.info("Sorting map");
		Map<String, BigDecimal> sorted = MapUtil.sortByValue(occurences);

		logger.info("Saving results to file " + output);
		writeResultToFile(output, sorted, -1);
	}

	public void extractMostCommonWords(File output, List<String> stopWords, int topWordsCount)
	{
		logger.info("Extracting the {} most common words", topWordsCount);

		Map<String, BigDecimal> occurences = new HashMap<>();

		int page = 0;
		int pageSize = 1000;
		long articleSize = repository.count();
		long pageCount = articleSize / pageSize;

		logger.info("There are {} articles to be processed (using pagesize of {}, total pages {})", articleSize, pageSize, pageCount);

		while (page <= pageCount)
		{
			logger.info("Fetching page ({}, {}) of articles", page, pageSize);
			Page<Article> articles = repository.findAll(new PageRequest(page, pageSize));

			if (articles.getNumberOfElements() < pageSize)
				logger.info("Could not get correct number of articles for page ({}, {}). Only {} returned", page, pageSize, articles.getSize());

			for (Article article : articles)
			{
				String[] words = WordUtil.getWords(article.getContent());

				for (String word : words)
				{
					if (word == null || word.trim().length() <= 1)
						continue;

					// normalization to lowercase
					word = word.toLowerCase();

					// remove stopwords
					if (stopWords.contains(word))
						continue;

					if (occurences.containsKey(word))
						occurences.put(word, occurences.get(word).add(BigDecimal.ONE));
					else
						occurences.put(word, BigDecimal.ONE);
				}
			}

			page++;
		}

		logger.info("Sorting map");
		Map<String, BigDecimal> sorted = MapUtil.sortByValue(occurences);

		logger.info("Saving results to file " + output);
		writeResultToFile(output, sorted, topWordsCount);
	}

	void writeResultToFile(File output, Map<String, BigDecimal> occurences, int topWordsCount)
	{
		Path newFile = Paths.get(output.getAbsolutePath());
		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			int counter = 0;
			for (String word : occurences.keySet())
			{
				if (topWordsCount != -1 && counter++ >= topWordsCount)
					break;

				writer.append(word + ":" + occurences.get(word) + "\n");
			}

			logger.info("Results saved to " + newFile);
		}
		catch (IOException ex)
		{
			logger.error("Could not write occurences to file " + output, ex);
		}
	}
}
