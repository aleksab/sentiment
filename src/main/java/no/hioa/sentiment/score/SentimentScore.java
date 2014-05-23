package no.hioa.sentiment.score;

import java.util.List;

public interface SentimentScore
{
	/**
	 * Calculate sentiment score based on sentimenlist. Returns list of scores.
	 * 
	 * @param sentimentList
	 * @return
	 */
	public List<Score> getSentimentScore(List<SentimentWord> sentimentList);
}
