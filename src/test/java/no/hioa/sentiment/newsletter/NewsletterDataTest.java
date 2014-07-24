package no.hioa.sentiment.newsletter;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NewsletterDataTest
{
	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void testIsWithinBlock() throws Exception
	{
		NewsletterData data = new NewsletterData();
		Assert.assertFalse(data.isWithinBlock("This is a very short text without any meaning", "short", "is", 2));
		Assert.assertTrue(data.isWithinBlock("This is a very short text without any meaning", "short", "is", 3));
		Assert.assertTrue(data.isWithinBlock("This is a very short text without any meaning", "short", "is", 30));
		Assert.assertFalse(data.isWithinBlock("This is a very short text without any meaning", "short", "nothere", 2));
	}
}
