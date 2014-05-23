package no.hioa.sentiment.score;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Default implementation of sentiment score.
 */
public class DefaultSentimentScore implements SentimentScore
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	private MongoOperations		mongoOperations;

	public DefaultSentimentScore(Corpus corpus) throws UnknownHostException
	{
		this.mongoOperations = MongoProvider.getMongoProvider(corpus);
	}

	/**
	 * Score all reviews in the movie review corpus. This works when the database size is not too big, otherwise we need to use paging instead of
	 * fetching all.
	 */
	@Override
	public List<Score> getSentimentScore(List<SentimentWord> sentimentList)
	{
		List<Review> reviews = mongoOperations.findAll(Review.class);
		List<Score> scores = new LinkedList<>();

		for (Review review : reviews)
		{
			scores.add(calculateSimpleSentimentScore(sentimentList, review));
		}

		return scores;
	}

	/**
	 * A very simple scoring implementation. We basically go through every word and see if we have a sentiment score for that word. If we do, we add
	 * it to the total sentiment score.
	 * 
	 * @param sentimentList
	 *            list of sentiment words
	 * @param review
	 *            review we want to score sentiment for
	 * @return
	 */
	Score calculateSimpleSentimentScore(List<SentimentWord> sentimentList, Review review)
	{
		logger.info("Calculating sentiment score for review {}", review.getId());

		BigDecimal sentimentScore = BigDecimal.ZERO;

		// Split every word by space
		String[] words = StringUtils.split(review.getContent(), " ");

		for (String word : words)
		{
			// remove typical sentence enders since we do a simple comparison against the word
			word = StringUtils.replaceChars(word, "!?()\"%&-.,", "");

			for (SentimentWord sWord : sentimentList)
			{
				if (word.equalsIgnoreCase(sWord.getWord()))
				{
					sentimentScore = sentimentScore.add(sWord.getRating());
					break; // we assume that a sentiment word can only occure once
				}
			}
		}

		return new Score(review.getId(), review.getRating(), sentimentScore);
	}
}
