package no.hioa.sentiment.filmweb;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FilmwebPmiTest
{
	private FilmwebPmi	pmi	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/bootstrap.xml");
		pmi = context.getBean(FilmwebPmi.class);
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
	public void testCalculateBasePmi() throws Exception
	{
		BigDecimal result = pmi.calculateBasePmi(new BigDecimal("0.25"), new BigDecimal("0.75"), new BigDecimal("0.75"));
		Assert.assertEquals(new BigDecimal("-1.169907").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculatePmi() throws Exception
	{
		BigDecimal result = pmi.calculatePmi("bra", "super", -1);
		Assert.assertEquals(new BigDecimal("0.2764729").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculatePmi2() throws Exception
	{
		BigDecimal result = pmi.calculatePmi("glimrende", "nydelig", -1);
		Assert.assertEquals(new BigDecimal("0.2764729").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculateNearPmi() throws Exception
	{
		BigDecimal result = pmi.calculatePmi("bra", "super", 10);
		Assert.assertEquals(new BigDecimal("-3.2863042").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculateSoPmi() throws Exception
	{
		List<String> pWords = pmi.getPositiveWords();
		List<String> nWords = pmi.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("fantastisk", pWords, nWords, -1);
		Assert.assertEquals(new BigDecimal("0.2764729").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testCalculateSoPmi2() throws Exception
	{
		List<String> pWords = pmi.getPositiveWords();
		List<String> nWords = pmi.getNegativeWords();

		BigDecimal result = pmi.calculateSoPmi("forferdelig", pWords, nWords, -1);
		Assert.assertEquals(new BigDecimal("0.2764729").floatValue(), result.floatValue(), 0);
	}

	@Test
	public void testIsWithinLimit() throws Exception
	{
		Assert.assertTrue(pmi.isWithinLimit("word1 word2 word3 word4 word5 word6 word7 word8 word9 word10", "word1", "word5", 4));
	}

	@Test
	public void testIsWithinLimit2() throws Exception
	{
		Assert.assertFalse(pmi.isWithinLimit("word1 word2 word3 word4 word5 word6 word7 word8 word9 word10", "word1", "word5", 3));
	}

	@Test
	public void testIsWithinLimit3() throws Exception
	{
		Assert.assertTrue(pmi.isWithinLimit("word1 word2 word3 word4 word5 word6 word7 word8 word9 word10", "word5", "word1", 4));
	}
}
