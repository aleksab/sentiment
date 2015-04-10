package no.hioa.mil;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class PostProcessing
{
	private static final Logger	logger			= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-input", description = "Folder to get unprocssed files", required = false)
	private String				inputFolder		= null;

	@Parameter(names = "-output", description = "Folder to save processed files", required = false)
	private String				outputFolder	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		PostProcessing main = new PostProcessing();
		new JCommander(main, args);
		//main.postProcessFolder();
		main.postProcessFile(new File("C:/Users/Aleksander/Desktop/milcom/AF/1.ocr.txt"), new File("C:/Users/Aleksander/Desktop/data/processed/1.processed.txt"));
	}

	public void postProcessFolder() throws Exception
	{
		for (File file : new File(inputFolder).listFiles())
		{
			if (StringUtils.contains(file.getName(), ".ocr.txt"))
			{
				String output = outputFolder + "/" + StringUtils.substringBefore(file.getName(), ".ocr") + ".processed.txt";
				postProcessFile(file, new File(output));
				logger.info("Processed file {}", file.getName());
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void postProcessFile(File input, File output) throws Exception
	{
		List<String> lines = FileUtils.readLines(input);

		StringBuffer bufferGeneral = new StringBuffer();
		String bufferColumn1 = "";
		String bufferColumn2 = "";

		for (String line : lines)
		{
			// we ignore empty lines
			if (StringUtils.strip(line).length() == 0)
				continue;

			String[] columns = getTwoColumns(line);

			bufferColumn1 = addColumnText(bufferColumn1, columns[0]);
			bufferColumn2 = addColumnText(bufferColumn2, columns[1]);
		}

		bufferColumn1 = removeNoneAlpha(bufferColumn1);
		bufferColumn2 = removeNoneAlpha(bufferColumn2);

		bufferGeneral.append(bufferColumn1).append("\n").append(bufferColumn2);

		// remove none alpha
		FileUtils.writeStringToFile(output, bufferGeneral.toString());
	}

	private String removeNoneAlpha(String buffer)
	{
		buffer = buffer.replaceAll("[^a-zA-Z\\s]", " ");

		while (buffer.contains("  "))
			buffer = buffer.replaceAll("  ", " ");

		return buffer;
	}

	private String addColumnText(String buffer, String line)
	{
		if (StringUtils.strip(line).length() == 0)
			return buffer;

		if (StringUtils.endsWithIgnoreCase(buffer.toString(), "¬"))
		{
			buffer = StringUtils.removeEnd(buffer, "¬");
			buffer += line;
		}
		else
			buffer += " " + line;

		return buffer;
	}

	private final String	COLUMN_SEPERATOR	= "        ";

	// we only support two columns.
	private String[] getTwoColumns(String line)
	{
		String[] splits = new String[2];

		if (!StringUtils.contains(line, COLUMN_SEPERATOR))
		{
			splits[0] = line;
			splits[1] = "";
		}
		else
		{
			if (line.startsWith(COLUMN_SEPERATOR))
			{
				splits[0] = "";
				splits[1] = line;
			}
			else
			{
				splits[0] = StringUtils.substringBefore(line, COLUMN_SEPERATOR);
				splits[1] = StringUtils.substringAfter(line, COLUMN_SEPERATOR);
			}
		}

		splits[0] = StringUtils.trim(splits[0]);
		splits[1] = StringUtils.trim(splits[1]);

		return splits;
	}
}
