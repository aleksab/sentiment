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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.service.SeedProvider;
import no.hioa.sentiment.util.MapUtil;
import no.hioa.sentiment.util.WordUtil;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class FilmwebData
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");

	private MovieRepository repository;
	private String host = null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		new FilmwebData("localhost").extractMostCommonWords(new File("target/topwords.txt"), Collections.<String> emptyList(), -1);
		new FilmwebData("localhost").removeStopWords(new File("target/topwords.txt"), new File("target/topwords.stripped.txt"),
				SeedProvider.getStopWords());
	}
	
	public FilmwebData() throws UnknownHostException
	{
		
	}

	public FilmwebData(String host) throws UnknownHostException
	{
		this.host = host;
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(host, Corpus.MOVIE_REVIEWS);

		RepositoryFactorySupport factory = new MongoRepositoryFactory(mongoOperations);
		this.repository = factory.getRepository(MovieRepository.class);
	}

	public Review getReview(String id) throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(host, Corpus.MOVIE_REVIEWS);
		logger.info("Searching for review with id {}", id);
		return mongoOperations.findById(id, Review.class);
	}

	public List<Review> getReviewFromLink(String link) throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(host, Corpus.MOVIE_REVIEWS);
		logger.info("Searching for review with link {}", link);
		BasicQuery query = new BasicQuery("{ link : '" + link + "' }");
		return mongoOperations.find(query, Review.class);
	}

	public void extractMostCommonWords(File output, List<String> stopWords, int topWordsCount)
	{
		logger.info("Extracting the {} most common words", topWordsCount);

		Map<String, BigDecimal> occurences = new HashMap<>();

		int page = 0;
		int pageSize = 1000;
		long reviewSize = repository.count();
		long pageCount = reviewSize / pageSize;

		logger.info("There are {} reviews to be processed (using pagesize of {}, total pages {})", reviewSize, pageSize, pageCount);

		while (page <= pageCount)
		{
			logger.info("Fetching page ({}, {}) of reviews", page, pageSize);
			Page<Review> reviews = repository.findAll(new PageRequest(page, pageSize));

			if (reviews.getNumberOfElements() < pageSize)
				logger.info("Could not get correct number of reviews for page ({}, {}). Only {} returned", page, pageSize, reviews.getSize());

			for (Review review : reviews)
			{
				String[] words = WordUtil.getWords(review.getContent());

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
		} catch (Exception ex)
		{
			logger.error("Could not read content for file " + input.getAbsolutePath(), ex);
		}

		logger.info("Sorting map");
		Map<String, BigDecimal> sorted = MapUtil.sortByValue(occurences);

		logger.info("Saving results to file " + output);
		writeResultToFile(output, sorted, -1);
	}

	List<String> getFileContent(File file)
	{
		List<String> words = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "ISO-8859-1"))
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
		} catch (IOException ex)
		{
			logger.error("Could not write occurences to file " + output, ex);
		}
	}
}
