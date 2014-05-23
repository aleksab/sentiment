package no.hioa.sentiment.score;

import java.math.BigDecimal;

public class Score
{
	private String		reviewId;
	private int			reviewRating;
	private BigDecimal	sentimentScore;

	public Score(String reviewId, int reviewRating, BigDecimal sentimentScore)
	{
		super();
		this.reviewId = reviewId;
		this.reviewRating = reviewRating;
		this.sentimentScore = sentimentScore;
	}

	public String getReviewId()
	{
		return reviewId;
	}

	public void setReviewId(String reviewId)
	{
		this.reviewId = reviewId;
	}

	public int getReviewRating()
	{
		return reviewRating;
	}

	public void setReviewRating(int reviewRating)
	{
		this.reviewRating = reviewRating;
	}

	public BigDecimal getSentimentScore()
	{
		return sentimentScore;
	}

	public void setSentimentScore(BigDecimal sentimentScore)
	{
		this.sentimentScore = sentimentScore;
	}
}
