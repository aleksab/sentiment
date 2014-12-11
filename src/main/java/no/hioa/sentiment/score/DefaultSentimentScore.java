package no.hioa.sentiment.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.review.Review;
import no.hioa.sentiment.review.ReviewType;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.util.WordUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;

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
	public List<Score> getSentimentScore(ReviewType type, List<SentimentWord> sentimentList, List<String> shifters, boolean avgSum)
	{
		BasicQuery query = new BasicQuery("{ type : '" + type.getName().toUpperCase() + "' }");
		List<Review> reviews = mongoOperations.find(query, Review.class);
		List<Score> scores = new LinkedList<>();

		for (Review review : reviews)
		{
			scores.add(calculateSimpleSentimentScoreWithShifter(sentimentList, shifters, review, avgSum));
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
	Score calculateSimpleSentimentScoreWithShifter(List<SentimentWord> sentimentList, List<String> shifters, Review review, boolean avgSum)
	{
		logger.info("Calculating sentiment score for review {}", review.getId());

		BigDecimal sentimentScore = BigDecimal.ZERO.setScale(2);
		BigDecimal absSentimentScore = BigDecimal.ZERO.setScale(2);
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
					absSentimentScore = absSentimentScore.add(score.abs());
					
					// we assume that a sentiment word can only occur once
					break;
				}
			}
		}

		if (words.length > 0)
		{
			if (avgSum)
				sentimentScore = sentimentScore.divide(new BigDecimal(words.length), RoundingMode.HALF_EVEN);
			else if (absSentimentScore.compareTo(BigDecimal.ZERO) == 0)
				sentimentScore = BigDecimal.ZERO;
			else
				sentimentScore = sentimentScore.divide(absSentimentScore, RoundingMode.HALF_EVEN);			
		}

		return new Score(review.getId(), review.getRating(), sentimentScore);
	}
	
	String calculateComplexSentimentScoreWithShifter(List<SentimentWord> sentimentList, List<String> shifters, Review review)
	{
		logger.info("Calculating complex sentiment score for review {}", review.getId());

		String output = "";
		BigDecimal totalScore = BigDecimal.ZERO.setScale(5);		
		String[] words = WordUtil.getWords(review.getContent());

		boolean shouldShift = false;
		int wordsSinceShifter = -1;
		for (String word : words)
		{
			BigDecimal wordScore = BigDecimal.ZERO.setScale(2);
			
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
					wordScore = sWord.getRating();

					if (shouldShift && wordsSinceShifter <= 3)
						wordScore = wordScore.negate();
					
					totalScore = totalScore.add(wordScore);

					// we assume that a sentiment word can only occur once
					break;
				}
			}
			
			output += word + "(" + wordScore + ") ";
		}

		output += "\nTotal word score: " + totalScore;		
		if (words.length > 0)
			totalScore = totalScore.divide(new BigDecimal(words.length), RoundingMode.UP);

		output += "\nTotal words: " + words.length;
		output += "\nTotal score: " + totalScore;
		
		return output;
	}
}
