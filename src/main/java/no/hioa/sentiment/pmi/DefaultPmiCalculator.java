package no.hioa.sentiment.pmi;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.BasicQuery;

public class DefaultPmiCalculator implements PmiCalculator
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	private Corpus				corpus;
	private MongoOperations		mongoOperations;

	public DefaultPmiCalculator(Corpus corpus) throws UnknownHostException
	{
		this.corpus = corpus;
		this.mongoOperations = MongoProvider.getMongoProvider(corpus);
	}

	public BigDecimal calculatePmiForDocuments(String word, String seedWord, int maxDistance)
	{
		return BigDecimal.ZERO;
	}

	public BigDecimal calculatePmiForBlocks(String word, String seedWord, int maxDistance)
	{
		
		BigDecimal wordBlockOccurence = new BigDecimal(findWordDistanceInBlock(word, seedWord, maxDistance)).setScale(10);
		BigDecimal seedBlockOccurence = new BigDecimal(findWordOccurenceInBlock(seedWord, maxDistance)).setScale(10);
		BigDecimal wordOccurence = new BigDecimal(findWordOccurence(word)).setScale(10);
		BigDecimal totalWords = new BigDecimal("1038434278").setScale(10);

		BigDecimal dividend = wordBlockOccurence.divide(seedBlockOccurence, RoundingMode.UP);
		BigDecimal divisor = wordOccurence.divide(totalWords, RoundingMode.UP);
		BigDecimal result = dividend.divide(divisor, RoundingMode.CEILING);

		// TODO: this is not ideal and we might lose precision
		result = new BigDecimal(Math.log(result.floatValue()) / Math.log(2));

		return result;
	}

	long getTotalWords()
	{
		return 0;
	}

	@Override
	public BigDecimal calculateSoPmi(String word, List<String> pWords, List<String> nWords, int maxDistance)
	{
		BigDecimal totalHitsNearPositive = BigDecimal.ZERO;
		BigDecimal totalHitsNearNegative = BigDecimal.ZERO;
		BigDecimal totalHitsPositive = BigDecimal.ZERO;
		BigDecimal totalHitsNegative = BigDecimal.ZERO;

		for (String positive : pWords)
		{
			totalHitsNearPositive = totalHitsNearPositive.add(new BigDecimal(findWordDistance(word, positive, maxDistance)));
			totalHitsPositive = totalHitsPositive.add(new BigDecimal(findWordOccurence(positive)));
		}

		for (String negative : nWords)
		{
			totalHitsNearNegative = totalHitsNearNegative.add(new BigDecimal(findWordDistance(word, negative, maxDistance)));
			totalHitsNegative = totalHitsNegative.add(new BigDecimal(findWordOccurence(negative)));
		}

		logger.info("Total hits near positive words are {}", totalHitsNearPositive);
		logger.info("Total hits near negative words are {}", totalHitsNearNegative);
		logger.info("Total hits of positive words are {}", totalHitsPositive);
		logger.info("Total hits of negative words are {}", totalHitsNegative);

		BigDecimal dividend = totalHitsNearPositive.multiply(totalHitsPositive).setScale(5);
		if (dividend.compareTo(BigDecimal.ZERO) == 0)
			dividend = new BigDecimal("0.01");

		BigDecimal divisor = totalHitsNearNegative.multiply(totalHitsNegative).setScale(5);
		if (divisor.compareTo(BigDecimal.ZERO) == 0)
			divisor = new BigDecimal("0.01");

		BigDecimal result = dividend.divide(divisor, RoundingMode.CEILING);

		// TODO: this is not ideal and we might lose precision
		result = new BigDecimal(Math.log(result.floatValue()) / Math.log(2));
		logger.info("SO-PMI for word {} is {} with maxDistance {}", word, result, maxDistance);

		return result;
	}

	public WordDistance findWordDistances(String word1, String word2)
	{
		if (!mongoOperations.collectionExists(WordDistance.class))
			mongoOperations.createCollection(WordDistance.class);

		BasicQuery query = new BasicQuery("{ $or : [ { word1 : '" + word1 + "', word2 : '" + word2 + "' }, { word1 : '" + word2 + "', word2 : '"
				+ word1 + "' } ] }");
		WordDistance wordDistance = mongoOperations.findOne(query, WordDistance.class);

		if (wordDistance == null)
		{
			logger.info("Word {} and {} does not exists in lookup table", word1, word2);

			BasicQuery textQuery = new BasicQuery("{ $text: { $search: \"'" + word1 + "' '" + word2 + "'\" } }");
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map.js")).replaceAll("%WORD1%", word1).replaceAll(
					"%WORD2%", word2);
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
					reduceFunction, DistanceResult.class);

			Set<Long> distances = new HashSet<>();
			for (DistanceResult result : results)
			{
				// id in results is the distance while value is occurrences
				distances.add(result.getId());
			}

			wordDistance = new WordDistance(word1, word2, distances);
			mongoOperations.insert(wordDistance);
		}

		logger.info("Word {} and {} with these distance {} found in lookup table", word1, word2, wordDistance.getDistances());

		return wordDistance;
	}

	@Override
	public long findWordDistance(String word1, String word2, long maxDistance)
	{
		if (!mongoOperations.collectionExists(WordDistance.class))
			mongoOperations.createCollection(WordDistance.class);

		BasicQuery query = new BasicQuery("{ $or : [ { word1 : '" + word1 + "', word2 : '" + word2 + "' }, { word1 : '" + word2 + "', word2 : '"
				+ word1 + "' } ] }");
		WordDistance wordDistance = mongoOperations.findOne(query, WordDistance.class);

		if (wordDistance == null)
		{
			logger.info("Word {} and {} with max distance of {} does not exists in lookup table", word1, word2, maxDistance);

			BasicQuery textQuery = new BasicQuery("{ $text: { $search: \"'" + word1 + "' '" + word2 + "'\" } }");
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map.js")).replaceAll("%WORD1%", word1).replaceAll(
					"%WORD2%", word2);
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
					reduceFunction, DistanceResult.class);

			Set<Long> distances = new HashSet<>();
			for (DistanceResult result : results)
			{
				// id in results is the distance while value is occurrences
				distances.add(result.getId());
			}

			wordDistance = new WordDistance(word1, word2, distances);
			mongoOperations.insert(wordDistance);

			logger.info("Word {} and {} have these distances: {}", word1, word2, distances);
		}
		else
			logger.info("Word {} and {} with these distance {} found in lookup table", word1, word2, wordDistance.getDistances());

		int occurrences = 0;
		if (maxDistance == -1)
			occurrences = wordDistance.getDistances().size();
		else
		{
			for (Long distance : wordDistance.getDistances())
			{
				if (distance <= maxDistance)
					occurrences++;
			}
		}

		logger.info("Word {} and {} have {} occurences within distance of {}", word1, word2, occurrences, maxDistance);

		return occurrences;
	}

	public long findWordDistanceInBlock(String word1, String word2, long maxDistance)
	{
		BasicQuery textQuery = new BasicQuery("{ $text: { $search: \"'" + word1 + "' '" + word2 + "'\" } }");
		String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map.js")).replaceAll("%WORD1%", word1).replaceAll(
				"%WORD2%", word2);
		String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

		MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
				reduceFunction, DistanceResult.class);

		int occurrences = 0;
		for (DistanceResult result : results)
		{
			// id in results is the distance while value is occurrences
			if (result.getId() <= maxDistance)
				occurrences += result.getValue();
		}

		logger.info("Word {} and {} have {} occurences within distance of {}", word1, word2, occurrences, maxDistance);

		return occurrences;
	}

	/**
	 * Find how many times a word occurs with block length on either left or right side. If a word has a block with given length on both sides, it
	 * counts as two.
	 * 
	 * @param word
	 * @param textSpace
	 * @return
	 */
	long findWordOccurenceInBlock(String word, int blockLength)
	{
		BasicQuery textQuery = new BasicQuery("{ $text: { $search: '" + word + "' } }");
		String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map2.js")).replaceAll("%WORD%", word);
		String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

		MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
				reduceFunction, DistanceResult.class);

		int occurrences = 0;
		for (DistanceResult result : results)
		{
			// id in results is the distance while value is occurrences
			if (result.getId() <= blockLength)
				occurrences += result.getValue();
		}

		logger.info("Occurence for {} with block length {} is {}", word, blockLength, occurrences);

		return occurrences;
	}

	@Override
	public long findWordOccurence(String word)
	{
		if (!mongoOperations.collectionExists(WordOccurence.class))
			mongoOperations.createCollection(WordOccurence.class);

		BasicQuery query = new BasicQuery("{ word : '" + word + "' }");
		WordOccurence wordOccurence = mongoOperations.findOne(query, WordOccurence.class);

		if (wordOccurence == null)
		{
			logger.info("Word {} does not exists in lookup table", word);

			BasicQuery textQuery = new BasicQuery("{ $text: { $search: '" + word + "' } }");
			long wordCount = mongoOperations.count(textQuery, corpus.getCollectionContentClazz());
			wordOccurence = new WordOccurence(word, wordCount);
			mongoOperations.insert(wordOccurence);

			logger.info("Occurence for {} is {}", word, wordCount);
		}
		else
			logger.info("Word {} found in lookup table with occurence {}", word, wordOccurence.getOccurence());

		return wordOccurence.getOccurence();
	}

	private String getJsFileContent(File file)
	{
		StringBuffer buffer = new StringBuffer();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine();
				buffer.append(input + "\n");
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return buffer.toString();
	}

	@SuppressWarnings("unused")
	private class DistanceResult
	{
		private long	id;
		private long	value;

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public long getValue()
		{
			return value;
		}

		public void setValue(long value)
		{
			this.value = value;
		}

		@Override
		public String toString()
		{
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	@Override
	public BigDecimal calculatePmi(String word, String seedWord, int maxDistance)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
