package no.hioa.mil;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

public class Analyzer
{
	private static final Logger	logger	= LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		Analyzer main = new Analyzer();
		new JCommander(main, args);
		// main.analyzeSecurity(false, new
		// File("C:/Users/Aleksander/Desktop/milcom/AF"),"C:/Users/Aleksander/Desktop/data/classification/not-excised-af.txt");
		// main.analyzeSecurity(false, new
		// File("C:/Users/Aleksander/Desktop/milcom/CH"),"C:/Users/Aleksander/Desktop/data/classification/not-excised-ch.txt");
		// main.analyzeSecurity(false, new
		// File("C:/Users/Aleksander/Desktop/milcom/PH"),"C:/Users/Aleksander/Desktop/data/classification/not-excised-ph.txt");

		main.analyzeSecurity(true, new File("E:/Data/milcom/AF"), "E:/Data/milcom/classification/excised-af.txt");
		main.analyzeSecurity(true, new File("E:/Data/milcom/CH"), "E:/Data/milcom/classification/excised-ch.txt");
		main.analyzeSecurity(true, new File("E:/Data/milcom/PH"), "E:/Data/milcom/classification/excised-ph.txt");
	}

	@SuppressWarnings("unchecked")
	public void analyzeSecurity(boolean keepExcised, File inputFolder, String outputFile) throws Exception
	{
		HashMap<Integer, Classification> classifications = new HashMap<>();
		HashMap<Classification, Integer> counter = new HashMap<>();

		for (File file : inputFolder.listFiles())
		{
			if (StringUtils.contains(file.getName(), ".meta.txt"))
			{
				Integer fileName = Integer.valueOf(StringUtils.substringBefore(file.getName(), "."));

				List<String> lines = IOUtils.readLines(new FileReader(file));

				for (String line : lines)
				{
					if (StringUtils.contains(line, "Citation"))
					{
						List<Classification> matches = new LinkedList<>();

						line = line.replaceAll("]", " ");
						line = line.replaceAll("\n", " ");
						line = line.replaceAll(",", " ");

						for (Classification classification : Classification.values())
						{
							boolean foundMatch = false;
							for (String identify : classification.getMatchStrings())
							{
								if (StringUtils.containsIgnoreCase(line, identify))
								{
									foundMatch = true;
									break;
								}
							}

							for (String notInclude : classification.getDoNotMatchStrings())
							{
								if (StringUtils.containsIgnoreCase(line, notInclude))
								{
									foundMatch = false;
									break;
								}
							}

							if (foundMatch)
								matches.add(classification);
						}

						if (matches.isEmpty())
						{
							incrementHash(counter, Classification.UNKNOWN);

							// logger.info("Could not classify: {}", line);
							classifications.put(fileName, Classification.UNKNOWN);
						}
						else if (matches.size() == 2)
						{
							// logger.info("Excised: " + fileName);
							matches.remove(Classification.EXCISED);

							if (keepExcised)
							{
								incrementHash(counter, Classification.EXCISED);

								// logger.info("Putting double to excised: " +
								// Classification.EXCISED);
								classifications.put(fileName, Classification.EXCISED);
							}
							else
							{
								incrementHash(counter, matches.get(0));

								// logger.info("Putting double to not excised: "
								// + matches.get(0));
								classifications.put(fileName, matches.get(0));
							}
						}
						else if (matches.size() > 2)
						{
							incrementHash(counter, matches.get(0));

							logger.info("Multiple more matches: {} ({})", matches, line);
							classifications.put(fileName, matches.get(0));
						}
						else
						{
							incrementHash(counter, matches.get(0));

							classifications.put(fileName, matches.get(0));
						}
					}
				}
			}
		}

		logger.info("Total: {}", classifications.size());

		List<Integer> sortedKeys = new ArrayList<Integer>(classifications.keySet());
		Collections.sort(sortedKeys);

		String buffer = "";
		for (Integer key : sortedKeys)
		{
			buffer += key + ":" + classifications.get(key) + ",";
		}			

		buffer = StringUtils.substringBeforeLast(buffer, ",");
		FileUtils.writeStringToFile(new File(outputFile), buffer.toString());

		StringBuffer stats = new StringBuffer();
		for (Classification key : counter.keySet())
		{
			logger.info(key.name() + ": " + counter.get(key));
			stats.append(key.name()).append(": ").append(counter.get(key)).append("\n");
		}

		FileUtils.writeStringToFile(new File(outputFile + ".stats"), stats.toString());
	}

	private void incrementHash(HashMap<Classification, Integer> types, Classification key)
	{
		if (types.containsKey(key))
		{
			Integer value = types.get(key);
			types.put(key, value + 1);
		}
		else
			types.put(key, 1);
	}
}
