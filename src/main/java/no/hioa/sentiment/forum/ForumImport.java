package no.hioa.sentiment.forum;

import java.io.File;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.util.DatabaseUtilities;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class ForumImport
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-db", description = "Mongo database name")
	private String				dbName			= "forum";

	@Parameter(names = "-xml", description = "Path to xml file")
	private String				xmlFile;

	@Parameter(names = "-p", description = "Print statistics")
	private boolean				printStats		= false;

	@Parameter(names = "-host", description = "Host to mongo server")
	private String				mongoHost;

	@Parameter(names = "-username", description = "Username of mongo user")
	private String				mongoUsername;

	@Parameter(names = "-password", description = "Password for mongo user")
	private String				mongoPassword;

	@Parameter(names = "-authdb", description = "Name of database where user is defined")
	private String				mongoAuthDb		= "admin";

	@Parameter(names = "-noauth", description = "Do not use authentication")
	private boolean				noAuth			= true;

	private MongoOperations		mongoOperations	= null;
	private JdbcTemplate		jdbcTemplate	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		new ForumImport(args).generateXml();
	}

	public ForumImport(String[] args) throws UnknownHostException
	{
		JCommander commander = new JCommander(this, args);

		if (noAuth)
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, Corpus.FORUM_POSTS);
		else
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, dbName, mongoUsername, mongoPassword);

		if (printStats)
			printStats();
		else if (xmlFile == null)
			commander.usage();
		else
		{
			long result = insertXmlIntoMongo(new File(xmlFile));
			consoleLogger.info("{} forum posts have been imported", result);
		}

	}

	public void printStats()
	{
		long sites = mongoOperations.count(new Query(), Site.class);
		consoleLogger.info("Number of sites in {} is {}", dbName, sites);

		long forums = mongoOperations.count(new Query(), Forum.class);
		consoleLogger.info("Number of forums in {} is {}", dbName, forums);

		long topics = mongoOperations.count(new Query(), Topic.class);
		consoleLogger.info("Number of topics in {} is {}", dbName, topics);

		long posts = mongoOperations.count(new Query(), Post.class);
		consoleLogger.info("Number of posts in {} is {}", dbName, posts);

		BasicQuery query = new BasicQuery("{ rating : 6 }");
		Post post = mongoOperations.findOne(query, Post.class);
		consoleLogger.info(post.getSiteId() + ", " + post.getForumId() + ", " + post.getTopicId() + ", " + post.getAuthor() + " - "
				+ post.getContent());
	}

	public long insertXmlIntoMongo(File xmlFile)
	{
		
		return 0;
	}

	public void generateXml() throws Exception
	{
		jdbcTemplate = DatabaseUtilities.getMySqlTemplate("localhost", "nettrapport_forum", "root", "power27");

		List<Site> sites = getSites();
		consoleLogger.info("There are {} sites", sites.size());

		for (Site site : sites)
		{
			PrintWriter xmlWritter = new PrintWriter("target/" + site.getName() + "-data.xml", "UTF-8");
			xmlWritter.write(getXmlHeader());
			xmlWritter.write(getXmlStartTag("site"));
			xmlWritter.write(getXmlTag("name", site.getName()));
			xmlWritter.write(getXmlStartTag("forums"));

			List<Forum> forums = getForums(site.getId());
			consoleLogger.info("There are {} forums for site {}", forums.size(), site.getName());

			for (Forum forum : forums)
			{
				xmlWritter.write(getXmlStartTag("forum"));
				xmlWritter.write(getXmlTag("link", forum.getLink()));
				xmlWritter.write(getXmlTag("title", forum.getTitle()));

				consoleLogger.info("Adding topics for forum: {}", forum.getLink());
				xmlWritter.write(getXmlStartTag("topics"));
				printTopics(site.getId(), forum.getId(), xmlWritter);
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
				// siteId will not be part of xml
				return new Forum(rs.getInt("ForumId"), "", rs.getString("Link"), rs.getString("Title"));
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
}
