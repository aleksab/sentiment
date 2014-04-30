package no.hioa.sentiment.ws;

import static spark.Spark.get;
import static spark.Spark.setPort;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

public class TextWs
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");

	private int port;
	private String ping;

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		new TextWs(5300, "/sentiment/ping").startServer();
	}

	public TextWs(int port, String ping)
	{
		this.port = port;
		this.ping = ping;
	}

	public void startServer()
	{
		setPort(port);

		get(new Route(ping)
		{
			@Override
			public Object handle(Request request, Response response)
			{
				return "pong";
			}
		});
	}
}
