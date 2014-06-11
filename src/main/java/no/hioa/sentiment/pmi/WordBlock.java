package no.hioa.sentiment.pmi;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class WordBlock
{
	@Id
	private String			id;
	private String			word;
	private Map<Long, Long>	sizes;

	public WordBlock()
	{

	}

	public WordBlock(String word, Map<Long, Long> sizes)
	{
		super();		
		this.word = word;
		this.sizes = sizes;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getWord()
	{
		return word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}

	public Map<Long, Long> getSizes()
	{
		return sizes;
	}

	public void setSizes(Map<Long, Long> sizes)
	{
		this.sizes = sizes;
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
