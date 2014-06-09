package no.hioa.sentiment.testdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import no.hioa.sentiment.newsletter.Article;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;

public class TestData
{
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		TestData.populateTestData();
	}

	public static void populateTestData() throws UnknownHostException
	{
		MongoOperations mongoOperations = MongoProvider.getMongoProvider(Corpus.TEST_ARTICLES);

		if (mongoOperations.collectionExists(Article.class))
		{
			consoleLogger.info("Dropping collection");
			mongoOperations.dropCollection(Article.class);
		}

		mongoOperations.createCollection(Article.class);

		File folder = new File("src/test/resources/no/hioa/sentiment/testdata/");
		for (File file : folder.listFiles())
		{
			List<Article> articles = extractArticles(file);
			for (Article article : articles)
			{
				mongoOperations.insert(article);
			}
		}
	}

	private static List<Article> extractArticles(File file)
	{
		List<Article> articles = new LinkedList<>();
		BufferedReader reader = null;

		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
			String line = reader.readLine();
			while (line != null)
			{
				String link = StringUtils.substringAfter(line, "link: ").trim();
				String newspaper = StringUtils.substringAfter(reader.readLine(), "newspaper: ").trim();
				String dateString = StringUtils.substringAfter(reader.readLine(), "date: ").trim();

				SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
				Date date = fmt.parse(dateString);

				StringBuffer content = new StringBuffer();
				line = reader.readLine();
				while (line != null)
				{
					content.append(line + " ");
					line = reader.readLine();
				}

				articles.add(new Article(link, newspaper, date, content.toString()));
			}
		} catch (Exception ex)
		{
			consoleLogger.error("Could not read content from file " + file.getAbsolutePath(), ex);
		} finally
		{
			IOUtils.closeQuietly(reader);
		}

		return articles;
	}
}
