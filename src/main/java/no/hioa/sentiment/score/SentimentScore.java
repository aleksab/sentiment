package no.hioa.sentiment.score;

import java.util.List;

public interface SentimentScore
{
	/**
	 * Calculate sentiment score based on sentimentlist. We also consider
	 * sentiment shifter if they are one or two words before a sentiment word.
	 * Returns list of scores.
	 * 
	 * @param sentimentList
	 * @param shifters
	 * @return
	 */
	public List<Score> getSentimentScore(List<SentimentWord> sentimentList, List<String> shifters);
}
