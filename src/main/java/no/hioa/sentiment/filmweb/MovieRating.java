package no.hioa.sentiment.filmweb;

import org.springframework.data.annotation.Id;

public class MovieRating
{
	@Id
	private String id;

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
