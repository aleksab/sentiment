package no.hioa.sentiment.filmweb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "movie")
public class MovieXML
{
	private String link;
	private String title;
	private String originalTitle;
	private List<ReviewXML> reviews;

	public MovieXML()
	{
		super();
	}

	public MovieXML(String title, String originalTitle)
	{
		super();
		this.title = title;
		this.originalTitle = originalTitle;
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
	public List<ReviewXML> getReviews()
	{
		return reviews;
	}

	public void setReviews(List<ReviewXML> reviews)
	{
		this.reviews = reviews;
	}
}
