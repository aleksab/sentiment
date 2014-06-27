package no.hioa.sentiment.pmi;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

	public DefaultPmiCalculator(String host, Corpus corpus) throws UnknownHostException
	{
		this.corpus = corpus;
		this.mongoOperations = MongoProvider.getMongoProvider(host, corpus);
	}

	@Override
	public BigDecimal calculatePmi(String word, String seedWord, int maxDistance)
	{
		return null;// calculatePmiForBlocks(word, seedWord, maxDistance);
	}

	/**
	 * Calculate pmi between two words based on documents.
	 * 
	 * @param word
	 * @param seedWord
	 * @param maxDistance
	 * @return
	 */
	public BigDecimal calculatePmiForDocuments(String word, String seedWord, int maxDistance)
	{
		BigDecimal wordBlockOccurence = new BigDecimal(findWordDistance(word, seedWord, maxDistance)).setScale(10);
		BigDecimal wordOccurence = new BigDecimal(findWordOccurence(word)).setScale(10);
		BigDecimal seedOccurence = new BigDecimal(findWordOccurence(seedWord)).setScale(10);
		BigDecimal totalWords = new BigDecimal(getTotalWords());

		BigDecimal dividend = wordBlockOccurence.divide(totalWords, RoundingMode.UP);
		BigDecimal divisor = (wordOccurence.multiply(seedOccurence)).divide((totalWords.multiply(totalWords)), RoundingMode.UP);
		BigDecimal result = dividend.divide(divisor, RoundingMode.CEILING);

		// TODO: this is not ideal and we might lose precision
		result = new BigDecimal(Math.log(result.floatValue()) / Math.log(2));

		logger.info("PMI document for word {} and seed word {} with max distance {} is {}", word, seedWord, maxDistance, result);
		return result;
	}

	/**
	 * Calculate pmi between two words based on blocks. There might be several blocks within the same document.
	 * 
	 * @param word
	 * @param seedWord
	 * @param maxDistance
	 * @return
	 */
	public PmiResult calculatePmiForBlocks(String word, String seedWord, int maxDistance)
	{
		BigDecimal wordBlockOccurence = new BigDecimal(findWordDistance(word, seedWord, maxDistance)).setScale(5, RoundingMode.UP);
		BigDecimal seedBlockOccurence = new BigDecimal(findWordOccurenceWithBlock(seedWord, maxDistance) * maxDistance).setScale(5, RoundingMode.UP);
		BigDecimal wordOccurence = new BigDecimal(findWordOccurence(word)).setScale(5, RoundingMode.UP);
		BigDecimal totalWords = new BigDecimal(getTotalWords());

		BigDecimal dividend = BigDecimal.ZERO;
		BigDecimal divisor = BigDecimal.ZERO;
		BigDecimal result = BigDecimal.ZERO;

		if (wordBlockOccurence.compareTo(BigDecimal.ZERO) == 0 || seedBlockOccurence.compareTo(BigDecimal.ZERO) == 0
				|| wordOccurence.compareTo(BigDecimal.ZERO) == 0)
		{
			result = BigDecimal.ONE;
		}
		else
		{
			dividend = wordBlockOccurence.divide(seedBlockOccurence, RoundingMode.UP);
			divisor = wordOccurence.divide(totalWords, RoundingMode.UP);
			result = dividend.divide(divisor, RoundingMode.UP);
		}

		logger.info("dividend: {}", dividend);
		logger.info("divisor: {}", divisor);

		// TODO: this is not ideal and we might lose precision
		result = new BigDecimal(Math.log(result.floatValue()) / Math.log(2)).setScale(5, RoundingMode.UP);

		PmiResult pmiResult = new PmiResult(wordBlockOccurence, seedBlockOccurence, wordOccurence, totalWords, result);

		logger.info("PMI result for word {} and seed word {} with max distance {} is {}", word, seedWord, maxDistance, pmiResult);
		return pmiResult;
	}

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

	/**
	 * Find number of occurrence where two words are within a maximum distance of each other.
	 * 
	 * @param word1
	 * @param word2
	 * @param maxDistance
	 *            -1 means all possible distances
	 * @return
	 */
	public long findWordDistance(String word1, String word2, long maxDistance)
	{
		WordDistance wordDistance = findAllWordDistances(word1, word2);

		int occurrences = 0;
		for (Long size : wordDistance.getDistances().keySet())
		{
			if (size <= maxDistance || maxDistance == -1)
				occurrences += wordDistance.getDistances().get(size);
		}

		logger.info("Word {} and {} have {} occurences within distance of {}", word1, word2, occurrences, maxDistance);

		return occurrences;
	}

	/**
	 * Find all possible distances between two words. The result is stored in a lookup table for the next time.
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	@Override
	public WordDistance findAllWordDistances(String word1, String word2)
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
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map_distance.js")).replaceAll("%WORD1%", word1).replaceAll(
					"%WORD2%", word2);
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
					reduceFunction, DistanceResult.class);

			Map<Long, Long> distances = new HashMap<>();
			for (DistanceResult result : results)
			{
				// id in results is the distance while value is occurrences
				distances.put(result.getId(), result.getValue());
			}

			wordDistance = new WordDistance(word1, word2, distances);
			mongoOperations.insert(wordDistance);

			logger.info("Word {} and {} have {} occurences", word1, word2, distances.size());
		}
		else
			logger.info("Word {} and {} with occurences {} found in lookup table", word1, word2, wordDistance.getDistances().size());

		return wordDistance;
	}

	/**
	 * Find how many times a word occurs with block length on either left or right side. If a word has a block with given length on both sides, it
	 * counts as two.
	 * 
	 * @param word
	 * @param textSpace
	 * @return
	 */
	@Override
	public long findWordOccurenceWithBlock(String word, int blockLength)
	{
		WordBlock wordBlock = findAllWordOccurenceWithBlock(word);

		int occurrences = 0;
		for (Long size : wordBlock.getSizes().keySet())
		{
			if (blockLength <= size)
				occurrences += wordBlock.getSizes().get(size);
		}

		logger.info("Word {} has {} size with block length of {}", word, occurrences, blockLength);

		return occurrences;
	}

	/**
	 * Find all block sizes for a word. Counts both left and right side for a word.
	 * 
	 * @param word
	 * @return
	 */
	public WordBlock findAllWordOccurenceWithBlock(String word)
	{
		if (!mongoOperations.collectionExists(WordBlock.class))
			mongoOperations.createCollection(WordBlock.class);

		BasicQuery query = new BasicQuery("{ word : '" + word + "' }");
		WordBlock wordBlock = mongoOperations.findOne(query, WordBlock.class);

		if (wordBlock == null)
		{
			BasicQuery textQuery = new BasicQuery("{ $text: { $search: '" + word + "' } }");
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map_block.js")).replaceAll("%WORD%", word);
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
					reduceFunction, DistanceResult.class);

			Map<Long, Long> sizes = new HashMap<>();
			for (DistanceResult result : results)
			{
				// id in results is the distance while value is occurrences
				sizes.put(result.getId(), result.getValue());
			}

			wordBlock = new WordBlock(word, sizes);
			mongoOperations.insert(wordBlock);

			logger.info("There are {} occurences for {}", wordBlock.getSizes().size(), word);

		}
		else
			logger.info("Word {} found in lookup table with occurences {}", word, wordBlock.getSizes().size());

		return wordBlock;
	}

	/**
	 * Find occurrence of words in collection. The result is stored in a lookup table for the next time.
	 * 
	 * @param word
	 *            the word to find occurrence for.
	 * @return
	 */
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
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map_count.js")).replaceAll("%WORD%", word);
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(textQuery, corpus.getCollectionContentName(), mapFunction,
					reduceFunction, DistanceResult.class);

			long wordCount = 0;
			if (results.iterator().hasNext())
				wordCount = results.iterator().next().value;

			wordOccurence = new WordOccurence(word, wordCount);
			mongoOperations.insert(wordOccurence);

			logger.info("Occurence for {} is {}", word, wordCount);
		}
		else
			logger.info("Word {} found in lookup table with occurence {}", word, wordOccurence.getOccurence());

		return wordOccurence.getOccurence();
	}

	/**
	 * Get total words for collection. The result is stored in a lookup table for the next time.
	 * 
	 * @return
	 */
	public long getTotalWords()
	{
		if (!mongoOperations.collectionExists(WordOccurence.class))
			mongoOperations.createCollection(WordOccurence.class);

		BasicQuery query = new BasicQuery("{ word : '!TOTALWORDS!' }");
		WordOccurence wordOccurence = mongoOperations.findOne(query, WordOccurence.class);

		if (wordOccurence == null)
		{
			query = new BasicQuery("{ $where: \"this.content.length > 0\" }");
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map_words.js"));
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(query, corpus.getCollectionContentName(), mapFunction,
					reduceFunction, DistanceResult.class);

			long totalWords = results.iterator().next().value;
			wordOccurence = new WordOccurence("!TOTALWORDS!", totalWords);
			mongoOperations.insert(wordOccurence);

			logger.info("There are {} words in database", totalWords);
		}
		else
			logger.info("There are {} words in database", wordOccurence.getOccurence());

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

	public class PmiResult
	{
		public BigDecimal	wordBlockOccurence;
		public BigDecimal	seedBlockOccurence;
		public BigDecimal	wordOccurence;
		public BigDecimal	totalWords;
		public BigDecimal	result;

		public PmiResult(BigDecimal wordBlockOccurence, BigDecimal seedBlockOccurence, BigDecimal wordOccurence, BigDecimal totalWords,
				BigDecimal result)
		{
			super();
			this.wordBlockOccurence = wordBlockOccurence;
			this.seedBlockOccurence = seedBlockOccurence;
			this.wordOccurence = wordOccurence;
			this.totalWords = totalWords;
			this.result = result;
		}

		@Override
		public String toString()
		{
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}
}
