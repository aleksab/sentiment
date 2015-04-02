package no.hioa.mil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public enum Classification
{
	UNKNOWN("Unknown", new String[]
	{ "Classification Unknown", "Unknown" }), UNCLASSIFIED("Unclassified", "Unclassified"), CONFIDENTIAL("Confidential", new String[] { "Confidential" }, new String[] { "Fax"}), SECRET(
			"Secret", new String[]
			{ "Secret" }, new String[]
			{ "Secretary", "Top", "Secretarial", "Attachment" }), TOPSECRET(
					"Top Secret", new String[]
							{ "Top Secret" }, new String[]
							{ "Secretary", "Secretarial", "Attachment" }), NONCLASSIFIED("Non-Classified", "Non-Classified"), /* EXCISED("Excised", "Excised"), */ LIMITEDOFFICIAL("Limited Official",
			"Limited Official"), PUBLICUSE("Public Use", "Public Use"), RESTRICTED("Restricted",
			"Restricted");

	private String			name;
	private List<String>	identifyStrings;
	private List<String>	doNotIncludeStrings;

	Classification(String name, String identifyString)
	{
		this.name = name;
		this.identifyStrings = Collections.singletonList(identifyString);
		this.doNotIncludeStrings = new LinkedList<>();
	}

	Classification(String name, String[] strings)
	{
		this.name = name;
		this.identifyStrings = new LinkedList<>();
		this.doNotIncludeStrings = new LinkedList<>();

		for (String string : strings)
		{
			identifyStrings.add(string);
		}
	}

	Classification(String name, String[] strings, String[] notInclude)
	{
		this.name = name;
		this.identifyStrings = new LinkedList<>();
		this.doNotIncludeStrings = new LinkedList<>();

		for (String string : strings)
		{
			identifyStrings.add(string);
		}

		for (String string : notInclude)
		{
			doNotIncludeStrings.add(string);
		}
	}

	public String getName()
	{
		return name;
	}

	public List<String> getIdentifyStrings()
	{
		return identifyStrings;
	}

	public List<String> getDoNotIncludeStrings()
	{
		return doNotIncludeStrings;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public static String getPossibleValues()
	{
		String buffer = "";

		for (Classification value : Classification.values())
			buffer += value.getName() + ", ";

		return StringUtils.substringBeforeLast(buffer, ", ");
	}

	public static Classification getEnum(String name)
	{
		for (Classification re : Classification.values())
		{
			if (re.name.compareTo(name) == 0)
			{
				return re;
			}
		}
		throw new IllegalArgumentException("Invalid value: " + name);
	}
}
