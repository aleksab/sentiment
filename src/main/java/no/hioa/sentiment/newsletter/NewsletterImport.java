package no.hioa.sentiment.newsletter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

@SuppressWarnings("deprecation")
@Repository
public class NewsletterImport
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Autowired
	MongoOperations				mongoOperations;

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");

		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/bootstrap.xml");
		NewsletterImport data = context.getBean(NewsletterImport.class);
		// data.insertXmlIntoMongo(new File("C:/Development/workspace juno/Hioa - Crawler/target/result.xml"));
		long articles = data.importAllArticlesVersion1(new File("E:/Data/norsk_aviskorpus/1/20030116-20050403/"));

		// long articles = data.importAllArticles(new File("E:/Data/norsk_aviskorpus/2/"));
		consoleLogger.info("Number of articles extracted {}", articles);
	}

	public long importAllArticlesVersion1(File folder)
	{
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

		return totalArticles;
	}

	public long importAllArticlesVersion2(File folder)
	{
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
					logger.info("Extracting articles from {}", file.getAbsolutePath());
					List<Article> articles = extractArticlesVersion2(file);
					totalArticles += articles.size();

					for (Article article : articles)
					{
						mongoOperations.insert(article);
					}
				}
			}
		}

		return totalArticles;
	}

	List<Article> extractArticlesVersion1(File file)
	{
		List<Article> articles = new LinkedList<>();
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(file));
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
				while (line != null && !line.startsWith("<"))
				{
					content.append(StringUtils.substringBefore(line, "¶"));
					line = reader.readLine();
				}

				articles.add(new Article(link, newspaper, date, content.toString()));
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content from file " + file.getAbsolutePath(), ex);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		return articles;
	}

	List<Article> extractArticlesVersion2(File file)
	{
		List<Article> articles = new LinkedList<>();
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null)
			{
				String link = StringUtils.substringBefore(StringUtils.substringAfter(line, "##U #").trim(), ">");
				String newspaper = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##B ").trim(), ">");
				String year = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##A ").trim(), ">");
				String month = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##M ").trim(), ">");
				String day = StringUtils.substringBefore(StringUtils.substringAfter(reader.readLine(), "##D ").trim(), ">");

				SimpleDateFormat fmt = new SimpleDateFormat("yy.MM.dd");
				Date date = fmt.parse(year + "." + month + "." + day);

				StringBuffer content = new StringBuffer();
				line = reader.readLine();
				while (line != null && !line.startsWith("##"))
				{
					content.append(StringUtils.substringBefore(line, "¶"));
					line = reader.readLine();
				}

				articles.add(new Article(link, newspaper, date, content.toString()));
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content from file " + file.getAbsolutePath(), ex);
		}
		finally
		{
			IOUtils.closeQuietly(reader);
		}

		return articles;
	}
}
