package no.hioa.sentiment.pmi;

import java.math.BigDecimal;
import java.util.List;

import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.service.SeedProvider;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class DefaultPmiCalculatorTest
{
	private DefaultPmiCalculator	pmi	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		pmi = new DefaultPmiCalculator(Corpus.MOVIE_REVIEWS);
	}

	@Test
	public void testCalculateSoPmi() throws Exception
	{
		List<String> pWords = SeedProvider.getPositiveWords();
		List<String> nWords = SeedProvider.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("fantastisk", pWords, nWords, 10);
		Assert.assertEquals(new BigDecimal("0.6322682").floatValue(), result.floatValue(), 0);
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
		Assert.assertEquals(45, pmi.findWordOccurence("super"));
	}

	@Test
	public void testFindWordOccurence2() throws Exception
	{
		Assert.assertEquals(155, pmi.findWordOccurence("deilig"));
	}

	@Test
	@Ignore("Vil nok match because of stemmed words")
	public void testFindWordOccurence3() throws Exception
	{
		String word = "deilig";
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(Corpus.MOVIE_REVIEWS);
		BasicQuery textQuery = new BasicQuery("{ $text: { $search: '" + word + "' } }");

		List<Review> reviews1 = mongoOperations.find(textQuery, Review.class);
		List<Review> reviews2 = mongoOperations.find(new Query().addCriteria(Criteria.where("content").regex("\\b" + word + "\\b", "i")),
				Review.class);

		Assert.assertEquals(reviews1.size(), reviews2.size());
	}
}
