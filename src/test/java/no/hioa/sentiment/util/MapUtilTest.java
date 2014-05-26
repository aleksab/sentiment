package no.hioa.sentiment.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapUtilTest
{
	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
	}

	@Test
	public void testSortByValues() throws Exception
	{
		Map<String, BigDecimal> unsorted = new HashMap<>();
		unsorted.put("A", new BigDecimal(3));
		unsorted.put("B", new BigDecimal(1));
		unsorted.put("C", new BigDecimal(2));
		unsorted.put("D", new BigDecimal(4));
		unsorted.put("E", new BigDecimal(2));
		unsorted.put("F", new BigDecimal(2));

		Map<String, BigDecimal> sorted = MapUtil.sortByValue(unsorted);

		Iterator<String> it = sorted.keySet().iterator();
		Assert.assertEquals("D", it.next());
		Assert.assertEquals("A", it.next());
		Assert.assertEquals("E", it.next());
		Assert.assertEquals("F", it.next());
		Assert.assertEquals("C", it.next());
		Assert.assertEquals("B", it.next());

		Assert.assertEquals(new BigDecimal(3), sorted.get("A"));
		Assert.assertEquals(new BigDecimal(1), sorted.get("B"));
		Assert.assertEquals(new BigDecimal(2), sorted.get("C"));
		Assert.assertEquals(new BigDecimal(4), sorted.get("D"));
		Assert.assertEquals(new BigDecimal(2), sorted.get("E"));
		Assert.assertEquals(new BigDecimal(2), sorted.get("F"));
	}
}
