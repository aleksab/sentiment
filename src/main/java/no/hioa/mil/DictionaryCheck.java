package no.hioa.mil;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class DictionaryCheck
{
	private static final Logger	logger		= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-file", description = "File to check", required = true)
	private String				file		= null;

	@Parameter(names = "-dic", description = "Dictionary to check against", required = true)
	private String				dictionary	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		DictionaryCheck main = new DictionaryCheck();
		new JCommander(main, args);
		main.checkDictionary();
	}

	@SuppressWarnings("unchecked")
	public void checkDictionary() throws Exception
	{
		List<String> dicLines = FileUtils.readLines(new File(dictionary));
		String[] words = FileUtils.readFileToString(new File(file)).split(" ");

		int match = 0;
		int unmatch = 0;

		for (String word : words)
		{
			if (dicLines.contains(word))
				match++;
			else
				unmatch++;
		}

		logger.info("Matched: " + match);
		logger.info("Unmatched: " + unmatch);
	}
}
