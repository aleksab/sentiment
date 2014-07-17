package no.hioa.sentiment.service;

import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.newsletter.Article;
import no.hioa.sentiment.product.ProductReview;

import org.apache.commons.lang.StringUtils;

public enum Corpus
{
	MOVIE_REVIEWS("MOVIE_REVIEWS", "filmweb", "review", Review.class), PRODUCT_REVIEWS("PRODUCT_REVIEWS", "product", "review", ProductReview.class), NEWSPAPER_ARTICLES(
			"NEWSPAPER_ARTICLES", "newspaper", "article", Article.class), FORUM_POSTS("FORUM_POSTS", "forum", "post", Article.class), TEST_ARTICLES(
			"TEST_ARTICLES", "test", "article", Article.class);

	private String		name;
	private String		dbName;
	private String		dbCollectionContentName;
	private Class<?>	collectionContentClazz;

	Corpus(String name, String dbName, String dbCollectionContentName, Class<?> collectionContentClazz)
	{
		this.name = name;
		this.dbName = dbName;
		this.dbCollectionContentName = dbCollectionContentName;
		this.collectionContentClazz = collectionContentClazz;
	}

	public String getName()
	{
		return name;
	}

	public String getDbName()
	{
		return dbName;
	}

	public String getCollectionContentName()
	{
		return dbCollectionContentName;
	}

	public Class<?> getCollectionContentClazz()
	{
		return collectionContentClazz;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public static String getPossibleValues()
	{
		String buffer = "";

		for (Corpus value : Corpus.values())
			buffer += value.getName() + ", ";

		return StringUtils.substringBeforeLast(buffer, ", ");
	}

	public static Corpus getEnum(String name)
	{
		for (Corpus re : Corpus.values())
		{
			if (re.name.compareTo(name) == 0)
			{
				return re;
			}
		}
		throw new IllegalArgumentException("Invalid Corpus value: " + name);
	}
}
