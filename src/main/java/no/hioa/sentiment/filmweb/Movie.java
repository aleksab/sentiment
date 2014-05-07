package no.hioa.sentiment.filmweb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Movie
{
	@Id
	private String				id;
	private String				link;
	private String				title;
	private String				originalTitle;
	private List<ReviewContent>	reviews;

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

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
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

	@XmlElement(name = "review")
	@XmlElementWrapper(name = "reviews")
	public List<ReviewContent> getReviews()
	{
		return reviews;
	}

	public void setReviews(List<ReviewContent> reviews)
	{
		this.reviews = reviews;
	}
}
