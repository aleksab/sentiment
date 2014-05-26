package no.hioa.sentiment.util;

import org.apache.commons.lang.StringUtils;

public class WordUtil
{
	public static String[] getWords(String input)
	{
		input = input.replaceAll("[^a-zA-Z������\\s]", " ");
		return StringUtils.split(input, " ");
	}
}
