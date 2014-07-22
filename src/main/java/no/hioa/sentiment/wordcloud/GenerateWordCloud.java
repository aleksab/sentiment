package no.hioa.sentiment.wordcloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class GenerateWordCloud
{
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		new GenerateWordCloud().generateSoPMI(new File("src/main/resources/no/hioa/sentiment/wordcloud/pmi.d100.csv"), new File(
				"target/sopmi-d100.txt"));
	}

	public void generateSoPMI(File pmiFile, File soPmiFile) throws FileNotFoundException, UnsupportedEncodingException
	{
		List<String> soPmi = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(pmiFile), "ISO-8859-1"))
		{
			// skip first 4 lines
			for (int i = 0; i < 4; i++)
				scanner.nextLine();

			while (scanner.hasNextLine())
			{
				String[] input = scanner.nextLine().toLowerCase().split(",");
				String word = input[0];
				BigDecimal score = BigDecimal.ZERO.setScale(5);
				for (int i = 1; i <= 14; i++)
					score = score.add(new BigDecimal(input[i]));

				String soPmiScore = word + ":" + score;
				soPmi.add(soPmiScore);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		PrintWriter writter = new PrintWriter(soPmiFile, "ISO-8859-1");
		for (String pmi : soPmi)
		{
			writter.append(pmi + "\n");
		}
		writter.close();
	}
}
