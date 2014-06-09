package no.hioa.sentiment.score;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.testdata.TestData;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultSentimentScoreTest
{
	private DefaultSentimentScore score = null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		TestData.populateTestData();
		score = new DefaultSentimentScore(Corpus.TEST_ARTICLES);
	}

	@Test
	public void testGetSentimentScore() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("beste", new BigDecimal("5")));

		List<Score> result = score.getSentimentScore(sentimentList, Collections.<String> emptyList());
		Assert.assertEquals(5920, result.size());
		Assert.assertEquals(new BigDecimal("10"), result.get(0).getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "Dette er en super bra film som f�r maks brahet! Det er kjempe bra sier de!",
				"testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("0.63"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore2() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "Dette er en super bra film som f�r d�rlig uttelling, men elles ganske bra!",
				"testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("0.36"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore3() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review",
				"Dette er en veldig d�rlig film. Noe av det d�rligste jeg har sett faktisk. S� d�rlig er den!", "testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("-0.56"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore4() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "", "testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("0.00"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScoreWithShifter() throws Exception
	{
		List<String> shifters = new LinkedList<>();
		shifters.add("ikke");

		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "Dette er ikke en bra film! Den er skikkelig d�rlig!", "testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, shifters, review);
		Assert.assertEquals(new BigDecimal("-1.00"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScoreWithShifter2() throws Exception
	{
		List<String> shifters = new LinkedList<>();
		shifters.add("ikke");

		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "Dette er ikke ikke bra film! Den er skikkelig d�rlig!", "testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, shifters, review);
		Assert.assertEquals(new BigDecimal("0.00"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScoreWithShifter3() throws Exception
	{
		List<String> shifters = new LinkedList<>();
		shifters.add("ikke");

		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("d�rlig", new BigDecimal("-5")));
		Review review = new Review("1", "link", 5, "Test review", "For � ikke si at dette er en bra film! Den er skikkelig bra!", "testdomain");

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, shifters, review);
		Assert.assertEquals(new BigDecimal("0.72"), result.getSentimentScore());
	}
}
