package no.hioa.sentiment.pmi;

import java.math.BigDecimal;
import java.util.List;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.service.SeedProvider;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoOperations;

public class DefaultPmiCalculatorTest
{
	private DefaultPmiCalculator pmi = null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		pmi = new DefaultPmiCalculator(Corpus.MOVIE_REVIEWS);
	}

	@Test
	public void testCalculateSoPmiSpeed() throws Exception
	{
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();

		MongoOperations mongoOperations = MongoProvider.getMongoProvider(Corpus.MOVIE_REVIEWS);
		mongoOperations.dropCollection(WordDistance.class);
		mongoOperations.dropCollection(WordOccurence.class);

		long startTime = System.currentTimeMillis();
		BigDecimal result = pmi.calculateSoPmi("fantastisk", pWords, nWords, 10);
		Assert.assertEquals(new BigDecimal("1.0429525").floatValue(), result.floatValue(), 0);
		long elapsedTime = System.currentTimeMillis() - startTime;
		// movie
		// 146, 145, 147 (before index)
		// 146, 145, 147 (after index)
		// 5, 5, 5 (after index and text search)
		
		// newspaper
		// (before index)
		// (before index but with text search)
		// (after index and with text search)
		System.out.println("Seconds elapsed: " + elapsedTime / 1000);
	}

	@Test
	public void testCalculateSoPmi() throws Exception
	{
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("fantastisk", pWords, nWords, 10);
		Assert.assertEquals(new BigDecimal("1.0429525").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculateSoPmi2() throws Exception
	{
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("forferdelig", pWords, nWords, 10);
		Assert.assertEquals(new BigDecimal("0").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testFindWordDistance() throws Exception
	{
		Assert.assertEquals(2, pmi.findWordDistance("vakker", "deilig", 100));
	}

	@Test
	public void testFindWordDistance2() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordDistance("super", "deilig", 100));
		Assert.assertEquals(1, pmi.findWordDistance("deilig", "super", 100));
	}

	@Test
	public void testFindWordOccurence() throws Exception
	{
		Assert.assertEquals(52, pmi.findWordOccurence("super"));
	}

	@Test
	public void testFindWordOccurence2() throws Exception
	{
		Assert.assertEquals(140, pmi.findWordOccurence("deilig"));
	}
}
