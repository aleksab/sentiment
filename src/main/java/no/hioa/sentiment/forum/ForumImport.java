package no.hioa.sentiment.forum;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import no.hioa.sentiment.util.DatabaseUtilities;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class ForumImport
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private JdbcTemplate		jdbcTemplate	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		JdbcTemplate jdbcTemplate = DatabaseUtilities.getMySqlTemplate("localhost", "nettrapport_forum", "root", "power27");
		new ForumImport(jdbcTemplate).generateXml();
	}

	public ForumImport(JdbcTemplate jdbcTemplate)
	{
		this.jdbcTemplate = jdbcTemplate;
	}

	public void generateXml() throws Exception
	{
		List<Site> sites = getSites();
		consoleLogger.info("There are {} sites", sites.size());

		for (Site site : sites)
		{
			PrintWriter xmlWritter = new PrintWriter("target/" + site.name + "-data.xml", "UTF-8");
			xmlWritter.write(getXmlHeader());
			xmlWritter.write(getXmlStartTag("site"));
			xmlWritter.write(getXmlTag("name", site.name));
			xmlWritter.write(getXmlStartTag("forums"));

			List<Forum> forums = getForums(site.id);
			consoleLogger.info("There are {} forums for site {}", forums.size(), site.name);

			for (Forum forum : forums)
			{
				xmlWritter.write(getXmlStartTag("forum"));
				xmlWritter.write(getXmlTag("link", forum.link));
				xmlWritter.write(getXmlTag("title", forum.title));

				consoleLogger.info("Adding topics for forum: {}", forum.title);
				xmlWritter.write(getXmlStartTag("topics"));
				printTopics(site.id, forum.id, xmlWritter);
				xmlWritter.write(getXmlEndTag("topics"));

				xmlWritter.write(getXmlEndTag("forum"));
			}

			xmlWritter.write(getXmlEndTag("forums"));
			xmlWritter.write(getXmlEndTag("site"));
			xmlWritter.close();
		}
	}

	private String getXmlHeader()
	{
		return "<?xml version=\"1.0\" encoding=\"Unicode\" standalone=\"yes\"?>\n";
	}

	private String getXmlStartTag(String tagName)
	{
		return "<" + tagName + ">\n";
	}

	private String getXmlEndTag(String tagName)
	{
		return "</" + tagName + ">\n";
	}

	private String getXmlTag(String tagName, String content)
	{
		return "<" + tagName + "><![CDATA[" + content + "]]></" + tagName + ">\n";
	}

	List<Site> getSites()
	{
		return jdbcTemplate.query("SELECT * FROM site", new ParameterizedRowMapper<Site>()
		{
			public Site mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				return new Site(rs.getInt("SiteId"), rs.getString("Name"));
			}
		});
	}

	List<Forum> getForums(int siteId)
	{
		return jdbcTemplate.query("SELECT * FROM forum WHERE SiteId=?", new ParameterizedRowMapper<Forum>()
		{
			public Forum mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				return new Forum(rs.getInt("ForumId"), rs.getString("Link"), rs.getString("Title"));
			}
		}, siteId);
	}

	void printTopics(final int siteId, final int forumId, final PrintWriter xmlWritter)
	{
		jdbcTemplate.query("SELECT * FROM topic WHERE SiteId=? AND ForumId=?", new RowCallbackHandler()
		{
			public void processRow(ResultSet rs) throws SQLException
			{
				int topicId = rs.getInt("TopicId");
				xmlWritter.write(getXmlStartTag("topic"));
				xmlWritter.write(getXmlTag("link", rs.getString("Link")));
				xmlWritter.write(getXmlTag("title", rs.getString("Title")));

				xmlWritter.write(getXmlStartTag("posts"));
				printPost(siteId, forumId, topicId, xmlWritter);
				xmlWritter.write(getXmlEndTag("posts"));

				xmlWritter.write(getXmlEndTag("topic"));
			}
		}, siteId, forumId);

		xmlWritter.flush();
	}

	void printPost(int siteId, int forumId, int topicId, final PrintWriter xmlWritter)
	{
		jdbcTemplate.query("SELECT * FROM post WHERE SiteId=? AND ForumId=? AND TopicId=?", new RowCallbackHandler()
		{
			public void processRow(ResultSet rs) throws SQLException
			{
				xmlWritter.write(getXmlStartTag("post"));
				xmlWritter.write(getXmlTag("author", rs.getString("Author")));
				xmlWritter.write(getXmlTag("date", rs.getString("Date")));
				xmlWritter.write(getXmlTag("Content", rs.getString("Content")));
				xmlWritter.write(getXmlEndTag("post"));
			}
		}, siteId, forumId, topicId);
	}

	private class Site
	{
		public int		id;
		public String	name;

		public Site(int id, String name)
		{
			super();
			this.id = id;
			this.name = name;
		}
	}

	private class Forum
	{
		public int		id;
		public String	link;
		public String	title;

		public Forum(int id, String link, String title)
		{
			super();
			this.id = id;
			this.link = link;
			this.title = title;
		}
	}
}
