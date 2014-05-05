package no.hioa.sentiment.filmweb;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

@Entity
@NoSql(dataFormat = DataFormatType.MAPPED)
public class MovieRating implements Serializable
{
	@Id
	@GeneratedValue
	@Field(name = "_id")
	private String id;

	@Basic
	private int rating;

	public MovieRating()
	{
		super();
	}
	
	public MovieRating(int rating)
	{
		super();
		this.rating = rating;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public int getRating()
	{
		return rating;
	}

	public void setRating(int rating)
	{
		this.rating = rating;
	}
}
