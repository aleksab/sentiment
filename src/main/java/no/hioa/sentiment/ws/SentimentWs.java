package no.hioa.sentiment.ws;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.setPort;

import java.io.File;
import java.io.FileInputStream;
import java.net.UnknownHostException;
import java.util.Scanner;

import no.hioa.sentiment.filmweb.FilmwebData;
import no.hioa.sentiment.filmweb.Review;
import no.hioa.sentiment.pmi.DefaultPmiCalculator;
import no.hioa.sentiment.score.DefaultSentimentScore;
import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.SeedProvider;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SentimentWs
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");

	@Parameter(names = "-port", description = "Webservice port")
	private int port = 5300;

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		new SentimentWs(args);
	}

	public SentimentWs(String[] args)
	{
		new JCommander(this, args);

		startServer();
	}

	public void startServer()
	{
		logger.info("Starting server on port {}", port);
		setPort(port);

		get(new Route("/sentiment/ping")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "pong";
			}
		});

		get(new Route("/sentiment/usage")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return getFileContent(new File("src/main/resources/no/hioa/sentiment/ws/usage.txt"));
			}
		});

		get(new JsonTransformer("/sentiment/seed/positive")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return SeedProvider.getPositiveWords();
			}
		});

		get(new JsonTransformer("/sentiment/seed/negative")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return SeedProvider.getNegativeWords();
			}
		});

		get(new JsonTransformer("/sentiment/seed/candidate")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return SeedProvider.getCandidateWords();
			}
		});

		post(new OccurenceWrapper("/sentiment/pmi/occurence")
		{
			@Override
			public OccurenceResponse handle(OccurenceRequest request) throws UnknownHostException
			{
				DefaultPmiCalculator pmi = new DefaultPmiCalculator("localhost", request.getCorpus());
				long occurence = pmi.findWordOccurence(request.getWord());
				return new OccurenceResponse(request.getWord(), occurence);
			}
		});

		post(new NearWrapper("/sentiment/pmi/near")
		{
			@Override
			public NearResponse handle(NearRequest request) throws UnknownHostException
			{
				DefaultPmiCalculator pmi = new DefaultPmiCalculator("localhost", request.getCorpus());
				long occurence = pmi.findWordDistance(request.getWord1(), request.getWord2(), request.getMaxDistance());
				return new NearResponse(request.getWord1(), request.getWord2(), request.getMaxDistance(), occurence);
			}
		});

		post(new JsonTransformer("/sentiment/pmi/sopmi")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "NOT SUPPORTED";
			}
		});

		post(new JsonTransformer("/sentiment/pmi/candidatepmi")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "NOT SUPPORTED";
			}
		});

		post(new SentimentScoreWrapper("/sentiment/score")
		{
			@Override
			public SentimentScoreResponse handle(SentimentScoreRequest request) throws UnknownHostException
			{
				DefaultSentimentScore score = new DefaultSentimentScore(Corpus.MOVIE_REVIEWS);
				return new SentimentScoreResponse(score.getSentimentScore(request.getSentimentList(), request.getShifterList()));
			}
		});

		get(new JsonTransformer("/filmweb/review/:id")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				String id = request.params("id");
				if (id == null || id.length() == 0)
					return "Invalid id";

				try
				{
					Review review = new FilmwebData().getReview(id);
					if (review == null)
						return "Could not find review by id " + id;
					else
						return review;
				} catch (UnknownHostException ex)
				{
					logger.error("Unknown error", ex);
					return "Could not connect to database";
				}
			}
		});

		logger.info("Server started");
	}

	private String getFileContent(File file)
	{
		StringBuffer buffer = new StringBuffer();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "ISO-8859-1"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine();
				buffer.append(input + "\n");
			}
		} catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return buffer.toString();
	}
}
