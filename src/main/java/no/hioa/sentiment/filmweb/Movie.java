package no.hioa.sentiment.filmweb;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Movie
{
	@Id
	private String				id;

	private String				title;

	private String				originalTitle;

	private List<MovieRating>	ratings	= new ArrayList<MovieRating>();

	public Movie()
	{
		super();
	}

	public Movie(String title, String originalTitle)
	{
		super();
		this.title = title;
		this.originalTitle = originalTitle;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getOriginalTitle()
	{
		return originalTitle;
	}

	public void setOriginalTitle(String originalTitle)
	{
		this.originalTitle = originalTitle;
	}

	public List<MovieRating> getRatings()
	{
		return ratings;
	}

	public void setRatings(List<MovieRating> ratings)
	{
		this.ratings = ratings;
	}
}
