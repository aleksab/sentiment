package no.hioa.sentiment.pmi;

import java.math.BigDecimal;
import java.math.RoundingMode;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.testdata.TestData;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultPmiCalculatorTest
{
	private DefaultPmiCalculator	pmi	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		TestData.populateTestData();
		pmi = new DefaultPmiCalculator("localhost", Corpus.TEST_ARTICLES);
	}

	@Test
	public void testGetTotalWords() throws Exception
	{
		Assert.assertEquals(91, pmi.getTotalWords());
	}

	@Test
	public void testFindWordOccurence() throws Exception
	{
		Assert.assertEquals(4, pmi.findWordOccurence("er"));
	}

	@Test
	public void testFindWordOccurence2() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordOccurence("fantastisk"));
	}

	@Test
	public void testFindWordOccurence3() throws Exception
	{
		Assert.assertEquals(2, pmi.findWordOccurence("absolutt"));
	}
	
	@Test
	public void testFindWordOccurence4() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordOccurence("nå"));
	}

	@Test
	public void testFindWordOccurenceWithBlock() throws Exception
	{
		Assert.assertEquals(2, pmi.findWordOccurenceWithBlock("fantastiske", 8));
	}

	@Test
	public void testFindWordOccurenceWithBlock2() throws Exception
	{
		Assert.assertEquals(0, pmi.findWordOccurenceWithBlock("fantastiske", 50));
	}

	@Test
	public void testFindWordOccurenceWithBlock3() throws Exception
	{
		Assert.assertEquals(2, pmi.findWordOccurenceWithBlock("fantastiske", 10));
	}

	@Test
	public void testFindAllWordDistances() throws Exception
	{
		Assert.assertEquals(1, pmi.findAllWordDistances("absolutt", "fantastiske").getDistances().size());
	}

	@Test
	public void testFindAllWordDistances2() throws Exception
	{
		Assert.assertEquals(4, pmi.findAllWordDistances("er", "men").getDistances().size());
	}
	
	@Test
	public void testFindAllWordDistances3() throws Exception
	{
		Assert.assertEquals(1, pmi.findAllWordDistances("nå", "fantastiske").getDistances().size());
	}

	@Test
	public void testFindWordDistance() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordDistance("absolutt", "positivt", 100));
	}

	@Test
	public void testFindWordDistance2() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordDistance("absolutt", "fantastiske", 4));
	}
	
	@Test
	public void testFindWordDistance3() throws Exception
	{
		Assert.assertEquals(1, pmi.findWordDistance("nå", "fantastiske", 10));
	}

	@Test
	public void testCalculatePmiForBlocks() throws Exception
	{
		Assert.assertEquals(new BigDecimal("1.1858"), pmi.calculatePmiForBlocks("absolutt", "fantastiske", 10).result.setScale(4, RoundingMode.UP));
	}

	@Test
	public void testCalculatePmiForBlocks2() throws Exception
	{
		Assert.assertEquals(new BigDecimal("0.0000"), pmi.calculatePmiForBlocks("absolutt", "fantastiske", 50).result.setScale(4, RoundingMode.UP));
	}

	@Test
	public void testCalculatePmiForBlocks3() throws Exception
	{
		Assert.assertEquals(new BigDecimal("0.0000"), pmi.calculatePmiForBlocks("asdasfafa", "fantastiske", 50).result.setScale(4, RoundingMode.UP));
	}
	
	@Test
	public void testCalculatePmiForBlocks4() throws Exception
	{
		Assert.assertEquals(new BigDecimal("2.1858"), pmi.calculatePmiForBlocks("nå", "fantastiske", 10).result.setScale(4, RoundingMode.UP));
	}

	@Test
	public void testCalculatePmiForDocuments() throws Exception
	{
		Assert.assertEquals(new BigDecimal("5.5078"), pmi.calculatePmiForDocuments("absolutt", "fantastiske", 10).setScale(4, RoundingMode.UP));
	}

	@Test
	public void testCalculatePmiForDocuments2() throws Exception
	{
		Assert.assertEquals(new BigDecimal("5.5078"), pmi.calculatePmiForDocuments("absolutt", "fantastiske", 50).setScale(4, RoundingMode.UP));
	}
}
