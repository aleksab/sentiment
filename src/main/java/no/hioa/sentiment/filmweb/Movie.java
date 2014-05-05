package no.hioa.sentiment.filmweb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

@Entity
@NoSql(dataFormat = DataFormatType.MAPPED)
public class Movie implements Serializable
{
	@Id
	@GeneratedValue
	@Field(name = "_id")
	private String id;

	@Basic
	private String rawReviewText;

	@Basic
	private String genere;

	@ElementCollection
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
