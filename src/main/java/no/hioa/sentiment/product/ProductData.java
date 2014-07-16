package no.hioa.sentiment.product;

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
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class ProductData
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	private ProductRepository repository;
	private String host = null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		//new ProductData("localhost").extractMostCommonWords(new File("target/topwords.txt"), Collections.<String> emptyList(), -1);
		//new ProductData("localhost").removeStopWords(new File("target/topwords.txt"), new File("target/topwords.stripped.txt"),SeedProvider.getStopWords());
		new ProductData("localhost").filterFile(new File("target/topwords.stripped.txt"), new File("target/adjektiver_fullform.txt"));
	}

	public ProductData(String host) throws UnknownHostException
	{
		this.host = host;
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(host, Corpus.PRODUCT_REVIEWS);

		RepositoryFactorySupport factory = new MongoRepositoryFactory(mongoOperations);
		this.repository = factory.getRepository(ProductRepository.class);
	}
	
	public void filterFile(File input, File filter) throws FileNotFoundException, UnsupportedEncodingException
	{
		List<String> words = getFileContent(input);
		List<String> keepFilter = getFileContent(filter);		
		
		PrintWriter output = new PrintWriter("target/filtered.txt", "UTF-8");
		
		for (String word : words)
		{			
			if (keepFilter.contains(word.split(":")[0]))
				output.write(word + "\n");
		}
		
		output.close();
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
			Page<ProductReview> reviews = repository.findAll(new PageRequest(page, pageSize));

			if (reviews.getNumberOfElements() < pageSize)
				logger.info("Could not get correct number of reviews for page ({}, {}). Only {} returned", page, pageSize, reviews.getSize());

			for (ProductReview review : reviews)
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
		}
		catch (Exception ex)
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
