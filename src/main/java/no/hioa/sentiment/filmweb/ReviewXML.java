package no.hioa.sentiment.filmweb;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "review")
public class ReviewXML
{
	private String link;
	private int rating;
	private String name;
	private String content;
	private String domain;

	public ReviewXML()
	{

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