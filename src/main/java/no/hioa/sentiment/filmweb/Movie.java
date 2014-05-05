package no.hioa.sentiment.filmweb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Movie
{
	@Id
	private String id;

	private String rawReviewText;

	private String genere;

	private List<MovieRating> orderLines = new ArrayList<MovieRating>();

	public Movie()
	{
		super();
	}

	public Movie(String rawReviewText, String genere)
	{
		super();
		this.rawReviewText = rawReviewText;
		this.genere = genere;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getRawReviewText()
	{
		return rawReviewText;
	}

	public void setRawReviewText(String rawReviewText)
	{
		this.rawReviewText = rawReviewText;
	}

	public String getGenere()
	{
		return genere;
	}

	public void setGenere(String genere)
	{
		this.genere = genere;
	}

	public List<MovieRating> getOrderLines()
	{
		return orderLines;
	}

	public void setOrderLines(List<MovieRating> orderLines)
	{
		this.orderLines = orderLines;
	}
}
