package no.hioa.sentiment.ws;

import java.util.List;

import no.hioa.sentiment.score.Score;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class SentimentScoreResponse
{
	private List<Score>	sentimentScore;

	public SentimentScoreResponse(List<Score> sentimentScore)
	{
		super();
		this.sentimentScore = sentimentScore;
	}

	public List<Score> getSentimentScore()
	{
		return sentimentScore;
	}

	public void setSentimentScore(List<Score> sentimentScore)
	{
		this.sentimentScore = sentimentScore;
	}

	@Override
	public boolean equals(Object obj)
	{
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
