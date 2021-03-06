package no.hioa.sentiment.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedProvider
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	public static List<String> getCandidateWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/service/candidate.txt"));
	}

	public static List<String> getPositiveWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/service/positive2.txt"));
	}

	public static List<String> getNegativeWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/service/negative2.txt"));
	}
	
	public static List<String> getStopWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/sentiment/service/stop-words-norwegian.txt"));
	}

	private static List<String> getFileContent(File file)
	{
		List<String> words = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "ISO-8859-1"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				words.add(input);
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return words;
	}
}
