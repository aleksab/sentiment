package no.hioa.mil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class DictionaryCheck
{
	private static final Logger	logger				= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-folder", description = "Folder to check", required = true)
	private String				folder				= null;

	@Parameter(names = "-dic", description = "Dictionary to check against", required = true)
	private String				dictionaryFolder	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		DictionaryCheck main = new DictionaryCheck();
		new JCommander(main, args);
		main.checkDictionary();
	}

	@SuppressWarnings("unchecked")
	public Set<String> buildDictionary(File folder) throws Exception
	{
		HashSet<String> dictionary = new HashSet<>();

		for (File file : folder.listFiles())
		{
			if (file.isFile())
			{
				logger.info("Adding {} to dictionary", file.getName());
				List<String> lines = FileUtils.readLines(file);
				dictionary.addAll(lines);
			}
		}

		logger.info("Dictionary contains {} words", dictionary.size());

		return dictionary;
	}

	public void checkDictionary() throws Exception
	{
		Set<String> dictionary = buildDictionary(new File(dictionaryFolder));

		for (File file : new File(folder).listFiles())
		{
			String[] words = FileUtils.readFileToString(file).split(" ");

			float match = 0;

			for (String word : words)
			{
				if (dictionary.contains(word))
					match++;
			}

			float matchPercentage = (match / (float) words.length) * 100;
			logger.info("Percentage for file {}: {} ({} / {})", file.getName(), matchPercentage, match, words.length);
		}
	}
}
