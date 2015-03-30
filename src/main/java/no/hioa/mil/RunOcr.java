package no.hioa.mil;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.ocrsdk.api.Client;
import com.ocrsdk.api.ProcessingSettings;
import com.ocrsdk.api.Task;

public class RunOcr
{
	private static final Logger	logger			= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-serverUrl", description = "Server of abby", required = true)
	private String				serverUrl		= null;

	@Parameter(names = "-applicationId", description = "Application id", required = true)
	private String				applicationId	= null;

	@Parameter(names = "-password", description = "Password", required = true)
	private String				password		= null;

	@Parameter(names = "-folder", description = "Folder to run ocr against", required = true)
	private String				folder			= null;

	private Client				restClient		= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		RunOcr main = new RunOcr();
		new JCommander(main, args);
		main.runOcr();
	}

	public void runOcr() throws Exception
	{
		restClient = new Client();
		restClient.serverUrl = serverUrl;
		restClient.applicationId = applicationId;
		restClient.password = password;

		for (File file : new File(folder).listFiles())
		{
			if (StringUtils.endsWithIgnoreCase(file.getName(), ".pdf"))
			{
				String text = getText(file.getAbsolutePath());
				if (text != null)
				{
					String name = StringUtils.substringBefore(file.getName(), ".pdf");
					String fileName = folder + "/" + name + ".ocr.txt";
					FileUtils.writeStringToFile(new File(fileName), text);
					logger.info("OCR done!");
				}
				else
					logger.info("Could not perform OCR");
			}
		}
	}

	private String getText(String path)
	{
		try
		{
			File tmpFile = File.createTempFile("result", ".txt");
			logger.info("Running OCR of {}", path);
			performRecognition("English", path, tmpFile.getAbsolutePath());

			return getFileContent(tmpFile);
		}
		catch (Exception ex)
		{
			logger.error("Could not get OCR text", ex);
			return null;
		}
	}

	private void performRecognition(String language, String filePath, String outputPath) throws Exception
	{
		ProcessingSettings.OutputFormat outputFormat = ProcessingSettings.OutputFormat.txt;
		ProcessingSettings settings = new ProcessingSettings();
		settings.setLanguage(language);
		settings.setOutputFormat(outputFormat);

		Task task = restClient.processImage(filePath, settings);;

		waitAndDownloadResult(task, outputPath);
	}

	/**
	 * Wait until task processing finishes
	 */
	private Task waitForCompletion(Task task) throws Exception
	{
		int counter = 0;
		// Note: it's recommended that your application waits
		// at least 2 seconds before making the first getTaskStatus request
		// and also between such requests for the same task.
		// Making requests more often will not improve your application performance.
		// Note: if your application queues several files and waits for them
		// it's recommended that you use listFinishedTasks instead (which is described
		// at http://ocrsdk.com/documentation/apireference/listFinishedTasks/).
		while (task.isTaskActive())
		{

			Thread.sleep(5000);
			System.out.println("Waiting..");
			task = restClient.getTaskStatus(task.Id);

			if (counter++ >= 25)
			{
				logger.info("Still waiting for OCR, so aborting!");
				break;
			}
		}
		return task;
	}

	/**
	 * Wait until task processing finishes and download result.
	 */
	private void waitAndDownloadResult(Task task, String outputPath) throws Exception
	{
		task = waitForCompletion(task);

		if (task.Status == Task.TaskStatus.Completed)
		{
			System.out.println("Downloading..");
			restClient.downloadResult(task, outputPath);
			System.out.println("Ready");
		}
		else if (task.Status == Task.TaskStatus.NotEnoughCredits)
		{
			System.out.println("Not enough credits to process document. " + "Please add more pages to your application's account.");
		}
		else
		{
			System.out.println("Task failed");
		}

	}

	private String getFileContent(File file)
	{
		StringBuffer buffer = new StringBuffer();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				buffer.append(input + "\n");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return buffer.toString();
	}
}
