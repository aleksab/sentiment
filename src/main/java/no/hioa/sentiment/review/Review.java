package no.hioa.sentiment.review;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Model for a review, both movie review and product review.
 */
@Document
public class Review
{
	@Id
	private String		id;

	private String		link;
	private int			rating;
	private String		title;
	private String		content;
	private String		author;
	private String		date;
	private ReviewType	type;

	public Review()
	{

	}

	public Review(String link, int rating, String title, String content, String author, String date, ReviewType type)
	{
		super();
		this.link = link;
		this.rating = rating;
		this.title = title;
		this.content = content;
		this.author = author;
		this.date = date;
		this.type = type;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public ReviewType getType()
	{
		return type;
	}

	public void setType(ReviewType type)
	{
		this.type = type;
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

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	@Override
	public boolean equals(Object obj)
	{
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
