package no.hioa.mil;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
		main.analyzeSecurity();
	}

	public void analyzeSecurity() throws Exception
	{
		HashMap<Classification, Integer> types = new HashMap<>();

		File folder = new File("C:/Users/Aleksander/Desktop/milcom/CH");

		for (File file : folder.listFiles())
		{
			if (StringUtils.contains(file.getName(), ".meta.txt"))
			{
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
							for (String identify : classification.getIdentifyStrings())
							{
								if (StringUtils.containsIgnoreCase(line, identify))
								{
									foundMatch = true;
									break;
								}
							}
							
							for (String notInclude : classification.getDoNotIncludeStrings())
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
							incrementHash(types, Classification.UNKNOWN);
							logger.info("Could not classify: {}", line);
						}
						else if (matches.size() > 1)
						{							
							logger.info("Multiple matches: {} ({})", matches, line);
						}
						else
							incrementHash(types, matches.get(0));
					}
				}
			}
		}

		int total = 0;
		for (Classification key : types.keySet())
		{
			total += types.get(key);
			logger.info(key.getName() + ": " + types.get(key));
		}
		
		logger.info("Total: {}", total);
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
