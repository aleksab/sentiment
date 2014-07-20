package no.hioa.sentiment.review;

import org.apache.commons.lang.StringUtils;

public enum ReviewType
{
	FILMWEB("Filmweb", "filmweb.no"), KOMPLETT("Komplett", "komplett.no"), MPX("Mpx", "mpx.no");

	private String	name;
	private String	url;

	ReviewType(String name, String url)
	{
		this.name = name;
		this.url = url;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
	
	public static String getValidType()
	{		
		String output = "";
		
		for (ReviewType re : ReviewType.values())
		{
			output += re.getName() + ", ";
		}
		
		output = StringUtils.removeEnd(output, ", ");
		
		return output;
	}

	public static ReviewType getEnum(String name)
	{
		for (ReviewType re : ReviewType.values())
		{
			if (re.name.equalsIgnoreCase(name) || re.url.equalsIgnoreCase(name))
			{
				return re;
			}
		}
		throw new IllegalArgumentException("Invalid product review value: " + name);
	}
}
