package no.hioa.sentiment.testdata;

import java.util.List;

import no.hioa.sentiment.newsletter.Article;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;

public class EncodingTest
{
	private MongoOperations	mongoOperations	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		TestData.populateTestData();
		mongoOperations = MongoProvider.getMongoProvider("localhost", Corpus.TEST_ARTICLES);		
	}

	@Test
	public void testFindNorwegianText() throws Exception
	{
		BasicQuery query = new BasicQuery("{ $text: { $search: 'nå' } }");
		Article article = mongoOperations.findOne(query, Article.class);
		Assert.assertNotNull(article);
	}
	
	@Test
	public void testFindNorwegianText2() throws Exception
	{		
		List<Article> articles = mongoOperations.findAll(Article.class);
		Assert.assertEquals(2, articles.size());		
	}
}
