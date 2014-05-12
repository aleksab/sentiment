package no.hioa.sentiment.filmweb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Review
{
	@Id
	private String id;
	private String movieId;
	private String link;
	private int rating;
	private String name;
	private String content;
	private String domain;

	public Review()
	{

	}

	public Review(String movieId, String link, int rating, String name, String content, String domain)
	{
		super();
		this.movieId = movieId;
		this.link = link;
		this.rating = rating;
		this.name = name;
		this.content = content;
		this.domain = domain;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getMovieId()
	{
		return movieId;
	}

	public void setMovieId(String movieId)
	{
		this.movieId = movieId;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public int getRating()
	{
		return rating;
	}

	public void setRating(int rating)
	{
		this.rating = rating;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}
}