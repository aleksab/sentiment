package no.hioa.sentiment.score;

import java.math.BigDecimal;

public class SentimentWord
{
	private String		word;
	private BigDecimal	rating;

	public SentimentWord(String word, BigDecimal rating)
	{
		super();
		this.word = word.toLowerCase();
		this.rating = rating;
	}

	public String getWord()
	{
		return word;
	}

	public void setWord(String word)
	{
		this.word = word.toLowerCase();
	}

	public BigDecimal getRating()
	{
		return rating;
	}

	public void setRating(BigDecimal rating)
	{
		this.rating = rating;
	}
}
