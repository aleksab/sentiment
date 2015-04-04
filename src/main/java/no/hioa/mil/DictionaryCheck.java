package no.hioa.mil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

	@Parameter(names = "-folder", description = "Folder to check", required = true)
	private String				folder		= null;

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

		for (File file : new File(folder).listFiles())
		{
			String[] words = FileUtils.readFileToString(file).split(" ");

			BigDecimal match = BigDecimal.ZERO;
			BigDecimal unmatch = BigDecimal.ZERO;

			for (String word : words)
			{
				if (dicLines.contains(word))
					match = match.add(BigDecimal.ONE);
				else
				{
					unmatch = unmatch.add(BigDecimal.ONE);
					// logger.info("Unmatched: " + word);
				}
			}

			match.setScale(5);
			BigDecimal matchPercentage = match.multiply(new BigDecimal(100).divide(new BigDecimal(words.length), RoundingMode.HALF_EVEN));

			// logger.info("Matched: " + match);
			// logger.info("Unmatched: " + unmatch);
			logger.info("Percentage for file {}: {}", file.getName(), matchPercentage);
		}
	}
}
