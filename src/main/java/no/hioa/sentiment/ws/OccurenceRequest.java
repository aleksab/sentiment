package no.hioa.sentiment.ws;

import no.hioa.sentiment.service.Corpus;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class OccurenceRequest implements RequestValidator
{
	private Corpus	corpus;
	private String	word;

	public Corpus getCorpus()
	{
		return corpus;
	}

	public void setCorpus(Corpus corpus)
	{
		this.corpus = corpus;
	}

	public String getWord()
	{
		return word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}

	@Override
	public void validateRequest() throws IllegalArgumentException
	{
		if (getCorpus() == null)
			throw new IllegalArgumentException("Missing or invalid corpus. Possible values are " + Corpus.getPossibleValues());
		if (getWord() == null || getWord().trim().length() == 0)
			throw new IllegalArgumentException("Missing word");
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
