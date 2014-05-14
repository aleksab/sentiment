package no.hioa.sentiment.filmweb;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilmwebPmiTest
{
	private FilmwebPmi	pmi	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		pmi = new FilmwebPmi();
	}

	@Test
	public void testGetPositiveWords() throws Exception
	{
		List<String> words = pmi.getPositiveWords();
		System.out.println(words);
		Assert.assertEquals(15, words.size());
	}

	@Test
	public void testGetNegativeWords() throws Exception
	{
		List<String> words = pmi.getNegativeWords();
		System.out.println(words);
		Assert.assertEquals(18, words.size());
	}

	@Test
	public void testCalculateSoPmi() throws Exception
	{
		List<String> pWords = pmi.getPositiveWords();
		List<String> nWords = pmi.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("fantastisk", pWords, nWords, 10);
		Assert.assertEquals(new BigDecimal("1.0429525").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculateSoPmi2() throws Exception
	{
		List<String> pWords = pmi.getPositiveWords();
		List<String> nWords = pmi.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("forferdelig", pWords, nWords, 10);
		Assert.assertEquals(new BigDecimal("0").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testFindWordDistance() throws Exception
	{
		Assert.assertEquals(2, pmi.findWordDistance("super", "film", 10));
	}

	@Test
	public void testFindWordDistance2() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordDistance("super", "deilig", 100));
	}

	@Test
	public void testFindWordOccurence() throws Exception
	{
		Assert.assertEquals(52, pmi.findWordOccurence("super"));
	}

	@Test
	public void testFindWordOccurence2() throws Exception
	{
		Assert.assertEquals(140, pmi.findWordOccurence("deilig"));
	}
}
