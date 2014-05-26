package no.hioa.sentiment.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.util.WordUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Default implementation of sentiment score.
 */
public class DefaultSentimentScore implements SentimentScore
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");

	private MongoOperations mongoOperations;

	public DefaultSentimentScore(Corpus corpus) throws UnknownHostException
	{
		this.mongoOperations = MongoProvider.getMongoProvider(corpus);
	}

	/**
	 * Score all reviews in the movie review corpus. This works when the
	 * database size is not too big, otherwise we need to use paging instead of
	 * fetching all.
	 */
	@Override
	public List<Score> getSentimentScore(List<SentimentWord> sentimentList, List<String> shifters)
	{
		List<Review> reviews = mongoOperations.findAll(Review.class);
		List<Score> scores = new LinkedList<>();

		for (Review review : reviews)
		{
			scores.add(calculateSimpleSentimentScoreWithShifter(sentimentList, shifters, review));
		}

		return scores;
	}

	/**
	 * A very simple scoring implementation. We basically go through every word
	 * and see if we have a sentiment score for that word. If we do, we add it
	 * to the total sentiment score. We also consider sentiment shifter if they
	 * are one or two words before a sentiment word.
	 * 
	 * @param sentimentList
	 * @param shifters
	 *            list of shifters
	 * @param review
	 * @return
	 */
	Score calculateSimpleSentimentScoreWithShifter(List<SentimentWord> sentimentList, List<String> shifters, Review review)
	{
		logger.info("Calculating sentiment score for review {}", review.getId());

		BigDecimal sentimentScore = BigDecimal.ZERO.setScale(2);
		String[] words = WordUtil.getWords(review.getContent());

		boolean shouldShift = false;
		int wordsSinceShifter = -1;
		for (String word : words)
		{
			// normalization to lowercase
			word = word.toLowerCase();

			// do we have a shifter
			for (String shifter : shifters)
			{
				if (word.equalsIgnoreCase(shifter.toLowerCase()))
				{
					shouldShift = !shouldShift;
					wordsSinceShifter = 0;
					break;
				}
			}

			if (wordsSinceShifter > 3)
				shouldShift = false;
			else if (wordsSinceShifter != -1)
				wordsSinceShifter++;

			for (SentimentWord sWord : sentimentList)
			{
				if (word.equalsIgnoreCase(sWord.getWord()))
				{
					BigDecimal score = sWord.getRating();

					if (shouldShift && wordsSinceShifter <= 3)
						score = score.negate();

					sentimentScore = sentimentScore.add(score);

					// we assume that a sentiment word can only occur once
					break;
				}
			}
		}

		if (words.length > 0)
			sentimentScore = sentimentScore.divide(new BigDecimal(words.length), RoundingMode.UP);

		return new Score(review.getId(), review.getRating(), sentimentScore);
	}
}
