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

public class DefaultSentimentScore implements SentimentScore
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	private MongoOperations		mongoOperations;

	public DefaultSentimentScore(Corpus corpus) throws UnknownHostException
	{
		this.mongoOperations = MongoProvider.getMongoProvider(corpus);
	}

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

	Score calculateSimpleSentimentScore(List<SentimentWord> sentimentList, Review review)
	{
		logger.info("Calculating sentiment score for review {}", review.getId());

		BigDecimal sentimentScore = BigDecimal.ZERO;
		String[] words = StringUtils.split(review.getContent(), " ");

		for (String word : words)
		{
			word = StringUtils.replaceChars(word, "!?()\"%&-.,", "");
			for (SentimentWord sWord : sentimentList)
			{
				if (word.equalsIgnoreCase(sWord.getWord()))
				{
					sentimentScore = sentimentScore.add(sWord.getRating());
				}
			}
		}

		return new Score(review.getId(), review.getRating(), sentimentScore);
	}
}
