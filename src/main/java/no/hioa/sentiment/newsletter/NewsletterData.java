package no.hioa.sentiment.newsletter;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.util.MapUtil;
import no.hioa.sentiment.util.WordUtil;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class NewsletterData
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	private ArticleRepository	repository;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new NewsletterData().extractMostCommonWords(new File("target/topwords.txt"), Collections.<String> emptyList(), -1);
		// new NewsletterData().extractMostCommonWords(new File("target/topwords.txt"), SeedProvider.getStopWords(), 10000);
		// new NewsletterData().countWords(new File("target/topwords.txt"));
		// new NewsletterData().extractMostCommonWords("5380d084196269ee08201197");
	}

	public NewsletterData() throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(Corpus.NEWSPAPER_ARTICLES);

		RepositoryFactorySupport factory = new MongoRepositoryFactory(mongoOperations);
		this.repository = factory.getRepository(ArticleRepository.class);
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

	public void extractMostCommonWords(String id) throws UnknownHostException
	{
		Article article = MongoProvider.getMongoProvider(Corpus.NEWSPAPER_ARTICLES).findById(id, Article.class);
		logger.info("Content before replace: " + article.getContent());
		String content = article.getContent().replaceAll("[^a-zA-ZøæåØÆÅ\\s]", " ");
		logger.info("Content after replace: " + content);
	}

	public void extractMostCommonWords(File output, List<String> stopWords, int topWordsCount)
	{
		logger.info("Extracting the {} most common words", topWordsCount);

		Map<String, BigDecimal> occurences = new HashMap<>();

		int page = 0;
		int pageSize = 1000;
		long articleSize = repository.count();
		logger.info("There are {} articles to be processed (using pagesize of {})", articleSize, pageSize);

		while (page < articleSize)
		{
			logger.info("Fetching page ({}, {}) of articles", page, pageSize);
			Page<Article> articles = repository.findAll(new PageRequest(page, pageSize));

			if (articles.getSize() < pageSize)
				logger.info("Could not get correct number of articles for page ({}, {}). Only {} returned", page, pageSize, articles.getSize());

			for (Article article : articles)
			{				
				String[] words = WordUtil.getWords(article.getContent());

				if (words.length < 50)
					logger.warn("Article {} has less than 50 words: {} (before replace: {})", article.getId(), words.length,
							article.getContent().length());

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

			page += pageSize;
		}

		logger.info("Sorting map of size {}", occurences.size());
		occurences = MapUtil.sortByValue(occurences);

		logger.info("Saving results to file " + output);
		writeResultToFile(output, occurences, topWordsCount);
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
