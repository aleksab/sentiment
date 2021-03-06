package no.hioa.sentiment.ws;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.ResponseTransformerRoute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class SentimentScoreWrapper extends ResponseTransformerRoute
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	protected SentimentScoreWrapper(String path)
	{
		super(path, "application/json");
	}

	@Override
	public String render(Object model)
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		return gson.toJson(model);
	}

	@Override
	public final Object handle(Request request, Response response)
	{
		SentimentScoreRequest incomingRequest = null;

		try
		{
			logger.info("Incoming request: {}", request.body());
			incomingRequest = translate(request.body());
			logger.info("Translated request: {}", incomingRequest);
		}
		catch (Exception ex)
		{
			logger.error("Could not translate request", ex);
			return "Could not parse incoming data. Error message: " + ex.getMessage();
		}

		try
		{
			incomingRequest.validateRequest();
		}
		catch (IllegalArgumentException ex)
		{
			logger.error("Could not validate request", ex);
			return "Could not validate request. Error message: " + ex.getMessage();
		}

		try
		{
			return handle(incomingRequest);
		}
		catch (Exception ex)
		{
			return "Unknown error: " + ex.getMessage();
		}
	}

	public abstract SentimentScoreResponse handle(SentimentScoreRequest request) throws UnknownHostException;

	private SentimentScoreRequest translate(String json)
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		return gson.fromJson(json, SentimentScoreRequest.class);
	}
}
