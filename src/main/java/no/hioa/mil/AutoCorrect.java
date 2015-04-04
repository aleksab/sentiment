package no.hioa.mil;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class AutoCorrect
{
	private static final Logger	logger				= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-input", description = "Folder to get files", required = true)
	private String				inputFolder			= null;

	@Parameter(names = "-output", description = "Folder to save autocorrected files", required = true)
	private String				outputFolder		= null;

	@Parameter(names = "-dic", description = "Dictionary to check against", required = true)
	private String				dictionaryFolder	= null;

	@Parameter(names = "-d", description = "Levenstein distance", required = false)
	private int					levensteinDistance	= 1;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		AutoCorrect main = new AutoCorrect();
		new JCommander(main, args);
		main.compareAutoCorrect();
	}

	public void compareAutoCorrect() throws Exception
	{
		Set<String> dictionary = buildDictionary(new File(dictionaryFolder));
		for (File file : new File(inputFolder).listFiles())
		{

			File output = new File(outputFolder + "/" + file.getName());
			autoCorrect(file, output);

			MatchResult resultBefore = checkDicionaryFile(dictionary, file);
			MatchResult resultAfter = checkDicionaryFile(dictionary, output);

			logger.info("Total percentage before: {} ({} / {})", ((resultBefore.match / resultBefore.size) * 100), resultBefore.match,
					resultBefore.size);
			logger.info("Total percentage after: {} ({} / {})", ((resultAfter.match / resultAfter.size) * 100), resultAfter.match, resultAfter.size);
		}
	}

	public void autoCorrect(File input, File output) throws Exception
	{
		Set<String> dictionary = buildDictionary(new File(dictionaryFolder));

		String[] words = FileUtils.readFileToString(input).split(" ");
		StringBuffer buffer = new StringBuffer();

		for (String word : words)
		{
			if (word.length() > 3 && !dictionary.contains(word))
			{
				int lowerLimit = word.length();
				int upperLimit = word.length();

				for (String dicWord : dictionary)
				{
					if (dicWord.length() >= lowerLimit && dicWord.length() <= upperLimit)
					{
						if (StringUtils.getLevenshteinDistance(word, dicWord) == levensteinDistance)
						{
							logger.info("Levenstein distance is 1, changing from {} to {}", word, dicWord);
							word = dicWord;
							break;
						}
					}
				}
			}

			buffer.append(word).append(" ");
		}

		FileUtils.writeStringToFile(output, buffer.toString());
	}

	private MatchResult checkDicionaryFile(Set<String> dictionary, File file) throws Exception
	{
		String[] words = FileUtils.readFileToString(file).split(" ");

		float match = 0;

		for (String word : words)
		{
			if (dictionary.contains(word))
				match++;
		}

		return new MatchResult(match, words.length);
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

				for (String line : lines)
					dictionary.add(line.toLowerCase());
			}
		}

		logger.info("Dictionary contains {} words", dictionary.size());

		return dictionary;
	}

	private class MatchResult
	{
		public double	match;
		public double	size;

		public MatchResult(double match, double size)
		{
			super();
			this.match = match;
			this.size = size;
		}
	}
}
