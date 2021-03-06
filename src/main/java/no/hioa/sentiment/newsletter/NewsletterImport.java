package no.hioa.sentiment.newsletter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

@SuppressWarnings("deprecation")
public class NewsletterImport
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-db", description = "Mongo database name")
	private String				dbName			= "newspaper";

	@Parameter(names = "-v1path", description = "Path to folder of version 1 files")
	private String				version1folder;

	@Parameter(names = "-v2path", description = "Path to folder of version 2 files")
	private String				version2folder;

	@Parameter(names = "-p", description = "Print statistics")
	private boolean				printStats		= false;

	@Parameter(names = "-initDb", description = "Setup database and collection")
	private boolean				initDb			= false;

	@Parameter(names = "-noauth", description = "Do not use authentication")
	private boolean				noAuth			= true;

	@Parameter(names = "-host", description = "Host to mongo server", required = true)
	private String				mongoHost;

	@Parameter(names = "-username", description = "Username of mongo user")
	private String				mongoUsername;

	@Parameter(names = "-password", description = "Password for mongo user")
	private String				mongoPassword;

	@Parameter(names = "-authdb", description = "Name of database where user is defined")
	private String				mongoAuthDb		= "admin";

	private MongoOperations		mongoOperations;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new NewsletterImport(args);
	}

	public NewsletterImport(String[] args) throws UnknownHostException
	{
		JCommander commander = new JCommander(this, args);

		if (noAuth)
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, Corpus.NEWSPAPER_ARTICLES);
		else
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, dbName, mongoUsername, mongoPassword);

		if (printStats)
			printStats();
		else if (initDb)
			initDb();
		else if (version1folder == null && version2folder == null)
			commander.usage();
		else
		{
			long result = 0;
			if (version1folder != null)
				result = importAllArticlesVersion1(new File(version1folder));
			else
				result = importAllArticlesVersion2(new File(version2folder));

			consoleLogger.info("{} articles have been imported", result);
		}
	}

	public void printStats()
	{
		long articles = mongoOperations.count(new Query(), Article.class);
		consoleLogger.info("Number of articles in {} is {}", dbName, articles);

		BasicQuery query = new BasicQuery("{ link : 'http://stavanger-aftenblad.no/nyheter/nytt/1998/1016/223813.html' }");
		List<Article> arts = mongoOperations.find(query, Article.class);
		for (Article art : arts)
			consoleLogger.info(art.getContent());
	}

	public void initDb()
	{
		if (!mongoOperations.collectionExists(Article.class))
		{
			mongoOperations.createCollection(Article.class);
			consoleLogger.info("Run this manually: db.runCommand( { enablesharding : \"newspaper\" } );");
			consoleLogger.info("Run this manually: db.runCommand( { shardcollection : \"newspaper.article\", key : { _id : 1 } } )");
			consoleLogger.info("Run this manually: db.article.ensureIndex( { content : \"text\" } )");
		}
		else
			consoleLogger.info("Collection article already exists! Make sure it's sharded and has index on text");
	}

	public long importAllArticlesVersion1(File folder)
	{
		if (!folder.exists())
		{
			consoleLogger.warn("Folder {} does not exists", folder);
			return 0;
		}

		if (!mongoOperations.collectionExists(Article.class))
		{
			mongoOperations.createCollection(Article.class);
		}

		long totalArticles = 0;
		for (File file : folder.listFiles())
		{
			List<Article> articles = extractArticlesVersion1(file);
			totalArticles += articles.size();

			for (Article article : articles)
			{
				mongoOperations.insert(article);
			}
		}

		// TODO: Add code for adding index to content

		return totalArticles;
	}

	public long importAllArticlesVersion2(File folder)
	{
		if (!folder.exists())
		{
			consoleLogger.warn("Folder {} does not exists", folder);
			return 0;
		}

		if (!mongoOperations.collectionExists(Article.class))
		{
			mongoOperations.createCollection(Article.class);
		}

		return insertAllArticlesVersion2(folder);
	}

	long insertAllArticlesVersion2(File folder)
	{
		long totalArticles = 0;
		for (File file : folder.listFiles())
		{
			if (file.isDirectory())
			{
				consoleLogger.info("Checking folder {}", file.getAbsolutePath());
				totalArticles += insertAllArticlesVersion2(file);
			}
			else
			{
				if (file.getName().endsWith("html4"))
				{
					List<Article> articles = extractArticlesVersion2(file);
					totalArticles += articles.size();

					for (Article article : articles)
					{
						mongoOperations.insert(article);
					}
				}
			}
		}

		// TODO: Add code for adding index to content

		return totalArticles;
	}

	List<Article> extractArticlesVersion1(File file)
	{
		List<Article> articles = new LinkedList<>();
		BufferedReader reader = null;
		double missingContent = 0;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
			String line = reader.readLine();
			while (line != null)
			{
				String link = StringUtils.substringBefore(StringUtils.substringAfter(line, "<U #").trim(), ">");
				reader.readLine(); // ignore '|'
				String newspaper = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "<B ").trim(), ">");
				String year = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "<A ").trim(), ">");
				String month = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "<M ").trim(), ">");
				String day = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "<D ").trim(), ">");

				if (!NumberUtils.isDigits(year))
					year = "98";

				if (!NumberUtils.isDigits(month))
					month = "01";

				if (!NumberUtils.isDigits(day))
					day = "01";

				SimpleDateFormat fmt = new SimpleDateFormat("yy.MM.dd");
				Date date = fmt.parse(year + "." + month + "." + day);

				StringBuffer content = new StringBuffer();
				line = reader.readLine();
				while (line != null && !line.startsWith("<U"))
				{
					// ignore if we get more dates before next article (with U)
					if (line.startsWith("<B") || line.startsWith("<B") || line.startsWith("<M") || line.startsWith("<D"))
						line = reader.readLine();
					else
					{
						line = line.replace("�", " ");
						content.append(line + " ");
						line = reader.readLine();
					}
				}

				if (content.length() == 0)
				{
					missingContent++;
				}
				else
					articles.add(new Article(link, newspaper, date, content.toString()));
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not read content from file " + file.getAbsolutePath(), ex);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		consoleLogger.warn("Number of article with empty content: {}", missingContent);

		return articles;
	}

	List<Article> extractArticlesVersion2(File file)
	{
		List<Article> articles = new LinkedList<>();
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
			String line = reader.readLine();
			while (line != null)
			{
				String link = StringUtils.substringBefore(StringUtils.substringAfter(line, "##U #").trim(), ">");
				String newspaper = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##B ").trim(), ">");
				String year = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##A ").trim(), ">");
				String month = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##M ").trim(), ">");
				String day = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##D ").trim(), ">");

				SimpleDateFormat fmt = new SimpleDateFormat("yy.MM.dd");
				Date date = null;

				try
				{
					fmt.parse(year + "." + month + "." + day);
				}
				catch (Exception ex)
				{
					date = fmt.parse("00.01.01");
				}

				StringBuffer content = new StringBuffer();
				line = reader.readLine();
				while (line != null && !line.startsWith("##U"))
				{
					// ignore if we get more dates before next article (with U)
					if (line.startsWith("##B") || line.startsWith("##A") || line.startsWith("##M") || line.startsWith("##D"))
						line = reader.readLine();
					else
					{
						line = line.replace("�", " ");
						content.append(line + " ");
						line = reader.readLine();
					}
				}

				if (content.length() > 0)
					articles.add(new Article(link, newspaper, date, content.toString()));
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not read content from file " + file.getAbsolutePath(), ex);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		return articles;
	}
}
