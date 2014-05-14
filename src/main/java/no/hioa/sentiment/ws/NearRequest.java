package no.hioa.sentiment.ws;

import no.hioa.sentiment.service.Corpus;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class NearRequest implements RequestValidator
{
	private Corpus	corpus;
	private String	word1;
	private String	word2;
	private long	maxDistance;

	public Corpus getCorpus()
	{
		return corpus;
	}

	public void setCorpus(Corpus corpus)
	{
		this.corpus = corpus;
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

	@Override
	public void validateRequest() throws IllegalArgumentException
	{
		if (getCorpus() == null)
			throw new IllegalArgumentException("Missing or invalid corpus. Possible values are " + Corpus.getPossibleValues());
		if (getWord1() == null || getWord1().trim().length() == 0)
			throw new IllegalArgumentException("Missing word1");
		if (getWord2() == null || getWord2().trim().length() == 0)
			throw new IllegalArgumentException("Missing word2");
		if (getMaxDistance() <= 0)
			throw new IllegalArgumentException("Max distance must be larger than 0");
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
