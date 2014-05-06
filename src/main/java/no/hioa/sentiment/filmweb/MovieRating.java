package no.hioa.sentiment.filmweb;

import org.springframework.data.annotation.Id;

public class MovieRating
{
	@Id
	private String	id;

	private String	url;

	private int		rating;

	private String	name;

	private String	content;

	public MovieRating()
	{
		super();
	}

	public MovieRating(String url, int rating, String name, String content)
	{
		super();
		this.url = url;
		this.rating = rating;
		this.name = name;
		this.content = content;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
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
}
