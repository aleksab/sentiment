package no.hioa.sentiment.service;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class WordDistance
{
	@Id
	private String	id;
	private String	word1;
	private String	word2;
	private long	maxDistance;
	private long	occurence;

	public WordDistance()
	{

	}

	public WordDistance(String word1, String word2, long maxDistance, long occurence)
	{
		super();
		this.word1 = word1;
		this.word2 = word2;
		this.maxDistance = maxDistance;
		this.occurence = occurence;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getWord1()
	{
		return word1;
	}

	public void setWord1(String word1)
	{
		this.word1 = word1;
	}

	public String getWord2()
	{
		return word2;
	}

	public void setWord2(String word2)
	{
		this.word2 = word2;
	}

	public long getMaxDistance()
	{
		return maxDistance;
	}

	public void setMaxDistance(long maxDistance)
	{
		this.maxDistance = maxDistance;
	}

	public long getOccurence()
	{
		return occurence;
	}

	public void setOccurence(long occurence)
	{
		this.occurence = occurence;
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
