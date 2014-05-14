package no.hioa.sentiment.ws;

import spark.ResponseTransformerRoute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JsonTransformer extends ResponseTransformerRoute
{
	protected JsonTransformer(String path)
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
}
