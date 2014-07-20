package no.hioa.sentiment.ws;

import java.util.List;

import no.hioa.sentiment.review.ReviewType;
import no.hioa.sentiment.score.SentimentWord;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.util.StringUtils;

public class SentimentScoreRequest implements RequestValidator
{
	private String				type;
	private List<SentimentWord>	sentimentList;
	private List<String>		shifterList;

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

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
		if (StringUtils.isEmpty(getType()))
			throw new IllegalArgumentException("Missing review type");
		try
		{
			ReviewType.getEnum(getType());			
		}
		catch (IllegalArgumentException ex)
		{
			throw new IllegalArgumentException("Invalid review type. Valid are: " + ReviewType.getValidType());
		}
		
		if (StringUtils.isEmpty(getSentimentList()))
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
