package no.hioa.sentiment.score;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.service.Corpus;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DefaultSentimentScoreTest
{
	private DefaultSentimentScore	score	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		score = new DefaultSentimentScore(Corpus.MOVIE_REVIEWS);
	}

	@Test
	@Ignore("Tied to database values")
	public void testCalculateSoPmi() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("beste", new BigDecimal("5")));

		List<Score> result = score.getSentimentScore(sentimentList);
		Assert.assertEquals(5920, result.size());
		Assert.assertEquals(new BigDecimal("10"), result.get(0).getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "Dette er en super bra film som får maks brahet! Det er kjempe bra sier de!",
				"testdomain");

		Score result = score.calculateSimpleSentimentScore(sentimentList, review);
		Assert.assertEquals(new BigDecimal("10"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore2() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "Dette er en super bra film som får dårlig uttelling, men elles ganske bra!",
				"testdomain");

		Score result = score.calculateSimpleSentimentScore(sentimentList, review);
		Assert.assertEquals(new BigDecimal("5"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore3() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review",
				"Dette er en veldig dårlig film. Noe av det dårligste jeg har sett faktisk. Så dårlig er den!", "testdomain");

		Score result = score.calculateSimpleSentimentScore(sentimentList, review);
		Assert.assertEquals(new BigDecimal("-10"), result.getSentimentScore());
	}
}
