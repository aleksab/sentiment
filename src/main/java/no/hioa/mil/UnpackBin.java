package no.hioa.mil;

import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tMap;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class UnpackBin
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-file", description = "File to unpack", required = true)
	private String				file			= null;

	@Parameter(names = "-output", description = "Folder where to unpack", required = true)
	private String				output			= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		UnpackBin main = new UnpackBin();
		new JCommander(main, args);
		main.unpackFile();
	}

	public void unpackFile() throws Exception
	{
		MessagePack msgpack = new MessagePack();
		FileInputStream input = new FileInputStream(file);

		Unpacker unpacker = msgpack.createUnpacker(input);
		Template<Map<String, Value>> mapTmpl = tMap(TString, TValue);

		int counter = 1;

		Map<String, Value> data = unpacker.read(mapTmpl);
		while (data != null)
		{

			String fileNameMeta = output + "/" + counter + ".meta.txt";
			String fileNamePdf = output + "/" + counter + ".pdf";

			writeMeta(fileNameMeta, data);
			writePdf(fileNamePdf, data);

			try
			{
				data = unpacker.read(mapTmpl);
			}
			catch (EOFException ex)
			{
				data = null;
			}

			counter++;
			consoleLogger.info("Done with {} files", counter);
		}
	}

	private void writeMeta(String fileName, Map<String, Value> data) throws Exception
	{
		StringBuffer buffer = new StringBuffer();

		for (String key : data.keySet())
		{
			if (key.equalsIgnoreCase("pdf") || key.equalsIgnoreCase("html") || key.equalsIgnoreCase("full text"))
				continue;

			buffer.append(key).append(": ").append(data.get(key)).append("\n");
		}

		FileUtils.writeStringToFile(new File(fileName), buffer.toString());
	}

	private void writePdf(String fileName, Map<String, Value> data) throws Exception
	{
		Value value = data.get("pdf");
		FileUtils.writeByteArrayToFile(new File(fileName), value.asRawValue().getByteArray());
	}
}
