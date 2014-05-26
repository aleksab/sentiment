package no.hioa.sentiment.ws;

import java.util.List;

import no.hioa.sentiment.score.SentimentWord;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class SentimentScoreRequest implements RequestValidator
{
	private List<SentimentWord> sentimentList;
	private List<String> shifterList;

	public List<SentimentWord> getSentimentList()
	{
		return sentimentList;
	}

	public void setSentimentList(List<SentimentWord> sentimentList)
	{
		this.sentimentList = sentimentList;
	}

	public List<String> getShifterList()
	{
		return shifterList;
	}

	public void setShifterList(List<String> shifterList)
	{
		this.shifterList = shifterList;
	}

	@Override
	public void validateRequest() throws IllegalArgumentException
	{
		if (getSentimentList() == null || getSentimentList().size() == 0)
			throw new IllegalArgumentException("Missing sentiment list");
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
