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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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

			Query regexQuery = new Query().addCriteria(Criteria.where("content").regex(
					"(\\b" + word1 + "\\b)(.*)(\\b" + word2 + "\\b)|(\\b" + word2 + "\\b)(.*)(\\b" + word1 + "\\b)", "isg"));
			String mapFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/map.js")).replaceAll("%WORD1%", word1).replaceAll(
					"%WORD2%", word2);
			String reduceFunction = getJsFileContent(new File("src/main/resources/no/hioa/sentiment/pmi/reduce.js"));

			MapReduceResults<DistanceResult> results = mongoOperations.mapReduce(regexQuery, corpus.getCollectionContentName(), mapFunction,
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

			long wordCount = mongoOperations.count(new Query().addCriteria(Criteria.where("content").regex("\\b" + word + "\\b", "i")),
					corpus.getCollectionContentClazz());
			wordOccurence = new WordOccurence(word, wordCount);
			mongoOperations.insert(wordOccurence);

			logger.info("Occurence for {} is {}", word, wordCount);
		}
		else
			logger.info("Word {} found in lookup table with occurence {}", word, wordOccurence.getOccurence());

		return wordOccurence.getOccurence();
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
}
