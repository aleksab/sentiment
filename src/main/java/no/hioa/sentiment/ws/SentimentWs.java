package no.hioa.sentiment.ws;

import static spark.Spark.get;
import static spark.Spark.setPort;
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
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	@Parameter(names = "-port", description = "Webservice port")
	private int					port	= 5300;

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

		get(new Route("/sentiment/seed/positive")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return SeedProvider.getPositiveWords();
			}
		});

		get(new Route("/sentiment/seed/negative")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return SeedProvider.getNegativeWords();
			}
		});

		get(new Route("/sentiment/seed/candidate")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return SeedProvider.getCandidateWords();
			}
		});

		get(new Route("/sentiment/pmi/occurence")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "NOT SUPPORTED";
			}
		});

		get(new Route("/sentiment/pmi/near")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "NOT SUPPORTED";
			}
		});

		get(new Route("/sentiment/pmi/sopmi")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "NOT SUPPORTED";
			}
		});

		get(new Route("/sentiment/pmi/candidatepmi")
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "NOT SUPPORTED";
			}
		});

		logger.info("Server started");
	}
}
