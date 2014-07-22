package no.hioa.sentiment.forum;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;
import no.hioa.sentiment.util.DatabaseUtilities;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.unbescape.html.HtmlEscape;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class ForumImport
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-db", description = "Mongo database name")
	private String				dbName			= "forum";

	@Parameter(names = "-xml", description = "Path to xml file")
	private String				xmlFile;

	@Parameter(names = "-p", description = "Print statistics")
	private boolean				printStats		= false;

	@Parameter(names = "-pxml", description = "Print xml statistics")
	private boolean				printXmlStats	= false;

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

	@Parameter(names = "-generate", description = "Generate xml from database")
	private boolean				generate		= false;

	@Parameter(names = "-initDb", description = "Setup database and collection")
	private boolean				initDb			= false;

	private MongoOperations		mongoOperations	= null;
	private JdbcTemplate		jdbcTemplate	= null;
	private XPathFactory		factory			= XPathFactory.newInstance();
	private XPath				xpath			= factory.newXPath();

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		new ForumImport(args);
	}

	public ForumImport(String[] args) throws Exception
	{
		JCommander commander = new JCommander(this, args);

		if (noAuth)
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, Corpus.FORUM_POSTS);
		else
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, dbName, mongoUsername, mongoPassword);

		if (printStats)
			printStats();
		else if (printXmlStats)
			printXmlStats();
		else if (initDb)
			initDb();
		else if (generate)
			generateXml();
		else if (xmlFile == null)
			commander.usage();
		else
		{
			long result = insertXmlFastIntoMongo(xmlFile);
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

	public void printXmlStats()
	{
		try
		{
			InputStream input = new FileInputStream(xmlFile);

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(input);
			doc.getDocumentElement().normalize();

			NodeList forumList = (NodeList) xpath.evaluate("//forums/forum", doc, XPathConstants.NODESET);
			consoleLogger.info("There are {} forums", forumList.getLength());

			NodeList topicList = (NodeList) xpath.evaluate("//topics/topic", doc, XPathConstants.NODESET);
			consoleLogger.info("There are {} topics", topicList.getLength());

			NodeList postList = (NodeList) xpath.evaluate("//posts/post", doc, XPathConstants.NODESET);
			consoleLogger.info("There are {} posts", postList.getLength());
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
		}
	}

	public void initDb()
	{
		if (!mongoOperations.collectionExists(Post.class))
		{
			mongoOperations.createCollection(Post.class);
			consoleLogger.info("Run this manually: db.runCommand( { enablesharding : \"forum\" } );");
			consoleLogger.info("Run this manually: db.runCommand( { shardcollection : \"forum.post\", key : { _id : 1 } } )");
			consoleLogger.info("Run this manually: db.post.ensureIndex( { content : \"text\" } )");
		}
		else
			consoleLogger.info("Collection post already exists! Make sure it's sharded and has index on text");
	}

	public long insertXmlFastIntoMongo(String xmlFile) throws Exception
	{
		try
		{
			final VTDGen vg = new VTDGen();
			vg.parseFile(xmlFile, false);

			final VTDNav vn = vg.getNav();
			final AutoPilot ap = new AutoPilot(vn);
			ap.selectXPath("//posts/post");

			int i = 0;
			while ((ap.evalXPath()) != -1)
			{
				String postAuthor = getNodeTextFast(vn, "author");
				String postDate = getNodeTextFast(vn, "date");
				String postContent = getNodeTextFast(vn, "content");

				Post post = new Post("0", "0", "0", postAuthor, postDate, postContent);
				mongoOperations.insert(post);

				i++;
				if (i % 1000 == 0)
					consoleLogger.info("Inserted {} posts", i);
			}

			return i;
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
			return 0;
		}
	}

	private String getNodeTextFast(VTDNav vn, String xpathString) throws Exception
	{
		AutoPilot ap = new AutoPilot(vn);
		ap.selectXPath(xpathString);

		String output = "";
		vn.push();
		if (ap.evalXPath() != -1)
			output = vn.toNormalizedString(vn.getText());
		ap.resetXPath();
		vn.pop();

		return output;
	}

	public long insertXmlIntoMongo(String xmlFile) throws Exception
	{
		try
		{
			InputStream input = new FileInputStream(xmlFile);

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(input);
			doc.getDocumentElement().normalize();

			boolean onlyInsertPots = true;

			if (onlyInsertPots)
			{
				NodeList postList = (NodeList) xpath.evaluate("//posts/post", doc, XPathConstants.NODESET);
				consoleLogger.info("Going to insert {} posts", postList.getLength());

				for (int i = 0; i < postList.getLength(); i++)
				{
					Element postNode = (Element) postList.item(i);
					String postAuthor = getNodeTextOrNull(postNode, "author");
					String postDate = getNodeTextOrNull(postNode, "date");
					String postContent = getNodeTextOrNull(postNode, "content");

					Post post = new Post("0", "0", "0", postAuthor, postDate, postContent);
					mongoOperations.insert(post);

					if (i % 1000 == 0)
						consoleLogger.info("Inserted {} posts", i);
				}

				return postList.getLength();
			}
			else
			{
				String siteName = getNodeTextOrNull(doc, "//site/name");
				Site site = new Site(siteName);
				mongoOperations.insert(site);
				consoleLogger.info("Inserted site {}", site);

				insertAllForums(doc, site.getId());
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
		}

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
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
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
				return new Site(rs.getString("SiteId"), cleanInput(rs.getString("Name")));
			}
		});
	}

	List<Forum> getForums(final String siteId)
	{
		return jdbcTemplate.query("SELECT * FROM forum WHERE SiteId=?", new ParameterizedRowMapper<Forum>()
		{
			public Forum mapRow(ResultSet rs, int rowNum) throws SQLException
			{
				return new Forum(rs.getString("ForumId"), siteId, rs.getString("Link"), cleanInput(rs.getString("Title")));
			}
		}, siteId);
	}

	void printTopics(final String siteId, final String forumId, final PrintWriter xmlWritter)
	{
		jdbcTemplate.query("SELECT * FROM topic WHERE SiteId=? AND ForumId=?", new RowCallbackHandler()
		{
			public void processRow(ResultSet rs) throws SQLException
			{
				String topicId = rs.getString("TopicId");
				xmlWritter.write(getXmlStartTag("topic"));
				xmlWritter.write(getXmlTag("link", rs.getString("Link")));
				xmlWritter.write(getXmlTag("title", cleanInput(rs.getString("Title"))));

				xmlWritter.write(getXmlStartTag("posts"));
				printPost(siteId, forumId, topicId, xmlWritter);
				xmlWritter.write(getXmlEndTag("posts"));

				xmlWritter.write(getXmlEndTag("topic"));
			}
		}, siteId, forumId);

		xmlWritter.flush();
	}

	void printPost(String siteId, String forumId, String topicId, final PrintWriter xmlWritter)
	{
		jdbcTemplate.query("SELECT * FROM post WHERE SiteId=? AND ForumId=? AND TopicId=?", new RowCallbackHandler()
		{
			public void processRow(ResultSet rs) throws SQLException
			{
				xmlWritter.write(getXmlStartTag("post"));
				xmlWritter.write(getXmlTag("author", cleanInput(rs.getString("Author"))));
				xmlWritter.write(getXmlTag("date", rs.getString("Date")));
				xmlWritter.write(getXmlTag("content", cleanInput(rs.getString("Content"))));
				xmlWritter.write(getXmlEndTag("post"));
			}
		}, siteId, forumId, topicId);
	}

	private String cleanInput(String input)
	{
		input = Jsoup.parse(input).body().text();
		input = Jsoup.clean(input, Whitelist.basic());
		input = HtmlEscape.unescapeHtml(input);

		StringBuffer out = new StringBuffer();
		char current;

		for (int i = 0; i < input.length(); i++)
		{
			current = input.charAt(i);
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}

		return out.toString();
	}

	private void insertAllForums(Document doc, String siteId) throws Exception
	{
		NodeList forumList = (NodeList) xpath.evaluate("//forums/forum", doc, XPathConstants.NODESET);
		consoleLogger.info("Going to insert {} forums", forumList.getLength());

		for (int i = 0; i < forumList.getLength(); i++)
		{
			Element forumNode = (Element) forumList.item(i);
			String forumLink = getNodeTextOrNull(forumNode, "link");
			String forumIitle = getNodeTextOrNull(forumNode, "title");

			Forum forum = new Forum(siteId, forumLink, forumIitle);
			mongoOperations.insert(forum);

			insertAllTopics(forumNode, forum);

			consoleLogger.info("Forum {} / {}", i, forumList.getLength());
		}
	}

	private void insertAllTopics(Element forumNode, Forum forum) throws Exception
	{
		NodeList topicList = (NodeList) xpath.evaluate("topics/topic", forumNode, XPathConstants.NODESET);

		for (int i = 0; i < topicList.getLength(); i++)
		{
			Element topicNode = (Element) topicList.item(i);
			String topicLink = getNodeTextOrNull(topicNode, "link");
			String topicTitle = getNodeTextOrNull(topicNode, "title");

			Topic topic = new Topic(forum.getSiteId(), forum.getId(), topicLink, topicTitle);
			mongoOperations.insert(topic);

			insertAllPosts(topicNode, topic);
		}
	}

	private void insertAllPosts(Element topicNode, Topic topic) throws Exception
	{
		NodeList postList = (NodeList) xpath.evaluate("posts/post", topicNode, XPathConstants.NODESET);

		for (int i = 0; i < postList.getLength(); i++)
		{
			Element postNode = (Element) postList.item(i);
			String postAuthor = getNodeTextOrNull(postNode, "author");
			String postDate = getNodeTextOrNull(postNode, "date");
			String postContent = getNodeTextOrNull(postNode, "content");

			Post post = new Post(topic.getSiteId(), topic.getForumId(), topic.getId(), postAuthor, postDate, postContent);
			mongoOperations.insert(post);
		}
	}

	private String getNodeTextOrNull(Object node, String xpathString) throws Exception
	{
		try
		{
			XPathExpression area = xpath.compile(xpathString);
			NodeList location = (NodeList) area.evaluate(node, XPathConstants.NODESET);
			Element locNode = (Element) location.item(0);
			return locNode.getTextContent();
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}
