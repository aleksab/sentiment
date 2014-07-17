package no.hioa.sentiment.forum;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Post
{
	@Id
	private int		id;
	private String	siteId;
	private String	forumId;
	private String	topicId;
	private String	author;
	private String	date;
	private String	content;

	public Post()
	{

	}

	public Post(String siteId, String forumId, String topicId, String author, String date, String content)
	{
		super();
		this.siteId = siteId;
		this.forumId = forumId;
		this.topicId = topicId;
		this.author = author;
		this.date = date;
		this.content = content;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}

	public String getForumId()
	{
		return forumId;
	}

	public void setForumId(String forumId)
	{
		this.forumId = forumId;
	}

	public String getTopicId()
	{
		return topicId;
	}

	public void setTopicId(String topicId)
	{
		this.topicId = topicId;
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

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	@Override
	public boolean equals(Object obj)
	{
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}