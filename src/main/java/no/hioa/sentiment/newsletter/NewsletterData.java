package no.hioa.sentiment.newsletter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.service.SeedProvider;
import no.hioa.sentiment.util.MapUtil;

import org.apache.commons.lang.StringUtils;
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

		new NewsletterData().extractMostCommonWords(new File("target/topwords.txt"), SeedProvider.getStopWords(), 10000);
	}

	public NewsletterData() throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(Corpus.NEWSPAPER_ARTICLES);

		RepositoryFactorySupport factory = new MongoRepositoryFactory(mongoOperations);
		this.repository = factory.getRepository(ArticleRepository.class);
	}

	public void extractMostCommonWords(File output, List<String> stopWords, int topWordsCount)
	{
		logger.info("Extracting the {} most common words", topWordsCount);

		Map<String, Integer> occurences = new HashMap<>();

		int page = 0;
		int pageSize = 500;
		long articleSize = repository.count();
		logger.info("There are {} articles to be processed (using pagesize of {})", articleSize, pageSize);

		while (page < articleSize)
		{
			logger.info("Fetching page ({}, {}) of articles", page, pageSize);
			Page<Article> articles = repository.findAll(new PageRequest(page, pageSize));

			for (Article article : articles)
			{
				String content = article.getContent().replaceAll("[^a-zA-ZøæåØÆÅ'\\s]", "");
				String[] words = StringUtils.split(content, " ");

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
						occurences.put(word, occurences.get(word) + 1);
					else
						occurences.put(word, 1);
				}
			}

			page += pageSize;
		}

		logger.info("Sorting map of size {}", occurences.size());
		occurences = MapUtil.sortByValue(occurences);

		logger.info("Saving results to file " + output);
		writeResultToFile(output, occurences, topWordsCount);
	}

	void writeResultToFile(File output, Map<String, Integer> occurences, int topWordsCount)
	{
		Path newFile = Paths.get(output.getAbsolutePath());
		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			int counter = 0;
			for (String word : occurences.keySet())
			{
				if (counter++ >= topWordsCount)
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
