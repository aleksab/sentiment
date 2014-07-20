package no.hioa.sentiment.score;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import no.hioa.sentiment.review.ReviewData;
import no.hioa.sentiment.review.Review;
import no.hioa.sentiment.review.ReviewType;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.testdata.TestData;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultSentimentScoreTest
{
	private DefaultSentimentScore	score	= null;

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

		List<Score> result = score.getSentimentScore(ReviewType.FILMWEB, sentimentList, Collections.<String> emptyList());
		Assert.assertEquals(0, result.size());
	}

	@Test
	public void testCalculateSimpleSentimentScore() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review", "Dette er en super bra film som får maks brahet! Det er kjempe bra sier de!", "author",
				"date", ReviewType.FILMWEB);

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("0.63"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore2() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review", "Dette er en super bra film som får dårlig uttelling, men elles ganske bra!", "author",
				"date", ReviewType.FILMWEB);

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("0.36"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore3() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review",
				"Dette er en veldig dårlig film. Noe av det dårligste jeg har sett faktisk. Så dårlig er den!", "author", "date", ReviewType.FILMWEB);

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, Collections.<String> emptyList(), review);
		Assert.assertEquals(new BigDecimal("-0.56"), result.getSentimentScore());
	}

	@Test
	public void testCalculateSimpleSentimentScore4() throws Exception
	{
		List<SentimentWord> sentimentList = new LinkedList<>();
		sentimentList.add(new SentimentWord("bra", new BigDecimal("5")));
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review", "", "author", "date", ReviewType.FILMWEB);

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
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review", "Dette er ikke en bra film! Den er skikkelig dårlig!", "author", "date",
				ReviewType.FILMWEB);

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
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review", "Dette er ikke ikke bra film! Den er skikkelig dårlig!", "author", "date",
				ReviewType.FILMWEB);

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
		sentimentList.add(new SentimentWord("dårlig", new BigDecimal("-5")));
		Review review = new Review("link", 5, "Test review", "For å ikke si at dette er en bra film! Den er skikkelig bra!", "author", "date",
				ReviewType.FILMWEB);

		Score result = score.calculateSimpleSentimentScoreWithShifter(sentimentList, shifters, review);
		Assert.assertEquals(new BigDecimal("0.72"), result.getSentimentScore());
	}

	@Test
	public void testCalculateComplexSentimentScoreWithShifter() throws Exception
	{
		ReviewData data = new ReviewData();
		List<Review> reviews = data.getReviewFromLink("http://www.side3.no/film/anmeldelser/article3659669.ece");
		Assert.assertEquals(1, reviews.size());

		List<SentimentWord> sentimentList = getSentimentWords(new File("src/test/resources/no/hioa/sentiment/score/sentiment_list01.txt"));

		List<String> shifters = new LinkedList<>();
		shifters.add("ikke");

		String result = score.calculateComplexSentimentScoreWithShifter(sentimentList, shifters, reviews.get(0));
		System.out.println(result);
		// Assert.assertEquals(new BigDecimal("0.05"), result.getSentimentScore());
	}

	private List<SentimentWord> getSentimentWords(File file)
	{
		List<SentimentWord> words = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			while (scanner.hasNextLine())
			{
				String[] input = scanner.nextLine().toLowerCase().split("\t");
				String word = input[0].trim();
				BigDecimal rating = new BigDecimal(input[1].trim());
				words.add(new SentimentWord(word, rating));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return words;
	}
}
