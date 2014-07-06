package no.hioa.sentiment.service;

import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SeedProviderTest
{
	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void getGetCandidateWords() throws Exception
	{
		List<String> words = SeedProvider.getCandidateWords();
		Assert.assertEquals(75, words.size());
	}

	@Test
	public void testGetPositiveWords() throws Exception
	{
		List<String> words = SeedProvider.getPositiveWords();
		Assert.assertEquals(7, words.size());
	}

	@Test
	public void testGetNegativeWords() throws Exception
	{
		List<String> words = SeedProvider.getNegativeWords();
		Assert.assertEquals(7, words.size());
	}
}
