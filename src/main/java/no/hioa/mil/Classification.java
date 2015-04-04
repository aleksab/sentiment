package no.hioa.mil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public enum Classification
{
	TOPSECRET(new String[]{ "Top Secret" }, new String[]{ "Secretary", "Secretarial", "Attachment" }),
	SECRET(new String[] { "Secret" }, new String[] { "Secretary", "Top", "Secretarial", "Attachment" }),
	CONFIDENTIAL(new String[] { "Confidential" }, new String[] { "Fax" }), 
	RESTRICTED("Restricted"),
	
	LIMITEDOFFICIAL(new String[] { "Limited Official", "Official Use" }),
	PUBLICUSE("Public Use"),	
	EXCISED("Excised"),
	
	UNCLASSIFIED("Unclassified"), 	
	NONCLASSIFIED("Non-Classified"), 
	UNKNOWN(new String[] { "Classification Unknown", "Unknown" });	
	
	private List<String>	matchStrings;
	private List<String>	doNotMatchStrings;

	Classification(String matchString)
	{
		this.matchStrings = Collections.singletonList(matchString);
		this.doNotMatchStrings = new LinkedList<>();
	}

	Classification(String[] matchStrings)
	{
		this.matchStrings = new LinkedList<>();
		this.doNotMatchStrings = new LinkedList<>();

		for (String string : matchStrings)
		{
			this.matchStrings.add(string);
		}
	}

	Classification(String[] matchStrings, String[] doNotMatchStrings)
	{
		this.matchStrings = new LinkedList<>();
		this.doNotMatchStrings = new LinkedList<>();

		for (String string : matchStrings)
		{
			this.matchStrings.add(string);
		}

		for (String string : doNotMatchStrings)
		{
			this.doNotMatchStrings.add(string);
		}
	}

	public List<String> getMatchStrings()
	{
		return matchStrings;
	}

	public List<String> getDoNotMatchStrings()
	{
		return doNotMatchStrings;
	}

	@Override
	public String toString()
	{
		return this.name();
	}

	public static String getPossibleValues()
	{
		String buffer = "";

		for (Classification value : Classification.values())
			buffer += value.name() + ", ";

		return StringUtils.substringBeforeLast(buffer, ", ");
	}

	public static Classification getEnum(String name)
	{
		for (Classification re : Classification.values())
		{
			if (re.name().compareTo(name) == 0)
			{
				return re;
			}
		}
		throw new IllegalArgumentException("Invalid value: " + name);
	}
}
