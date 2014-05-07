package no.hioa.sentiment.newsletter;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Article
{
	@Id
	private String	id;
	private String	link;
	private String	newspaper;
	private Date	date;
	private String	content;

	public Article()
	{
		super();
	}

	public Article(String link, String newspaper, Date date, String content)
	{
		super();
		this.link = link;
		this.newspaper = newspaper;
		this.date = date;
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

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getNewspaper()
	{
		return newspaper;
	}

	public void setNewspaper(String newspaper)
	{
		this.newspaper = newspaper;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
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
