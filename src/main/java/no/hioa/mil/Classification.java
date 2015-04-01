package no.hioa.mil;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public enum Classification
{
	UNKNOWN("Unknown", "Classification Unknown", "Unknown"), UNCLASSIFIED("Unclassified", "Unclassified"), CONFIDENTIAL("Confidential",
			"Confidential"), SECRET("Secret", "Secret"), NONCLASSIFIED("Non-Classified", "Non-Classified"), /* EXCISED("Excised", "Excised"), */
			LIMITEDOFFICIAL(
			"Limited Official", "Limited Official"), OFFICIALUSE("Official Use", "Official Use"), PUBLICUSE("Public Use", "Public Use"), RESTRICTED(
			"Restricted", "Restricted");

	private String			name;
	private List<String>	identifyStrings;

	Classification(String name, String identifyString)
	{
		this.name = name;
		this.identifyStrings = Collections.singletonList(identifyString);
	}

	Classification(String name, String... strings)
	{
		this.name = name;
		this.identifyStrings = new LinkedList<>();
		for (String string : strings)
		{
			identifyStrings.add(string);
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
