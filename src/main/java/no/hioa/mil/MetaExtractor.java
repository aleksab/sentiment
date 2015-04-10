package no.hioa.mil;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class MetaExtractor
{
	private static final Logger	logger			= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-input", description = "Folder to get unprocssed files", required = false)
	private String				inputFolder		= null;

	@Parameter(names = "-output", description = "Folder to save processed files", required = true)
	private String				outputFolder	= null;

	@Parameter(names = "-file", description = "File to process", required = false)
	private String				file			= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		MetaExtractor main = new MetaExtractor();
		new JCommander(main, args);

		if (main.shouldProcessFile())
			main.postProcessFile();
		else
			main.postProcessFolder();
	}

	public boolean shouldProcessFile()
	{
		return (file != null);
	}

	public void postProcessFolder() throws Exception
	{
		for (File file : new File(inputFolder).listFiles())
		{
			if (StringUtils.contains(file.getName(), ".meta.txt"))
			{
				String output = outputFolder + "/" + StringUtils.substringBefore(file.getName(), ".meta") + ".processed.txt";
				postProcessFile(file, new File(output));
				logger.info("Processed file {}", file.getName());
			}
		}
	}

	public void postProcessFile() throws Exception
	{
		File input = new File(file);
		String output = outputFolder + "/" + StringUtils.substringBefore(input.getName(), ".meta") + ".processed.txt";
		postProcessFile(input, new File(output));
	}

	@SuppressWarnings("unchecked")
	public void postProcessFile(File input, File output) throws Exception
	{
		List<String> lines = FileUtils.readLines(input);

		HashMap<String, String> categories = new HashMap<>();

		StringBuffer buffer = new StringBuffer();

		for (String line : lines)
		{
			String key = StringUtils.substringBefore(line, ":");
			String content = StringUtils.substringAfter(line, ":");
			categories.put(key, content);
		}

		for (String key : categories.keySet())
		{
			if (key.equalsIgnoreCase("Abstract") || key.equalsIgnoreCase("Subjects") || key.equalsIgnoreCase("Citation")
					|| key.equalsIgnoreCase("From") || key.equalsIgnoreCase("To") || key.equalsIgnoreCase("Individuals/Organizations Named")
					|| key.equalsIgnoreCase("Origin"))
			{
				String content = categories.get(key);
				content = processContent(content);
				buffer.append(content).append(" ");
			}
		}

		// write to file
		FileUtils.writeStringToFile(output, buffer.toString());
	}

	private String processContent(String buffer)
	{
		buffer = StringUtils.replace(buffer, "\\n", " ");

		buffer = buffer.replaceAll("[^a-zA-Z\\s]", " ");

		while (buffer.contains("  "))
			buffer = buffer.replaceAll("  ", " ");

		return buffer;
	}
}
