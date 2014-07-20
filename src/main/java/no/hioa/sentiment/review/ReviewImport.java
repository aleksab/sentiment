package no.hioa.sentiment.review;

import java.io.File;
import java.net.UnknownHostException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class ReviewImport
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-db", description = "Mongo database name")
	private String				dbName			= "review";

	@Parameter(names = "-xml", description = "Path to xml file")
	private String				xmlFile;

	@Parameter(names = "-p", description = "Print statistics")
	private boolean				printStats		= false;

	@Parameter(names = "-host", description = "Host to mongo server")
	private String				mongoHost;

	@Parameter(names = "-username", description = "Username of mongo user")
	private String				mongoUsername;

	@Parameter(names = "-password", description = "Password for mongo user")
	private String				mongoPassword;

	@Parameter(names = "-authdb", description = "Name of database where user is defined")
	private String				mongoAuthDb		= "admin";

	@Parameter(names = "-noauth", description = "Do not use authentication")
	private boolean				noAuth			= true;

	private MongoOperations		mongoOperations;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new ReviewImport(args);
	}

	public ReviewImport(String[] args) throws UnknownHostException
	{
		JCommander commander = new JCommander(this, args);

		if (noAuth)
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, Corpus.REVIEWS);
		else
			mongoOperations = MongoProvider.getMongoProvider(mongoHost, dbName, mongoUsername, mongoPassword);

		if (printStats)
			printStats();
		else if (xmlFile == null)
			commander.usage();
		else
		{
			long result = insertXmlIntoMongo(new File(xmlFile));
			consoleLogger.info("{} reviews have been imported", result);
		}
	}

	public void printStats()
	{
		long reviews = mongoOperations.count(new Query(), Review.class);
		consoleLogger.info("Number of reviews in {} is {}", dbName, reviews);

		BasicQuery query = new BasicQuery("{ rating : 6 }");
		Review review = mongoOperations.findOne(query, Review.class);
		consoleLogger.info(review.getRating() + " - " + review.getContent());
	}

	public long insertXmlIntoMongo(File xmlFile)
	{
		ReviewHeaderXML reviews = null;

		try
		{
			JAXBContext context = JAXBContext.newInstance(ReviewHeaderXML.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			reviews = (ReviewHeaderXML) unmarshaller.unmarshal(xmlFile);
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
			return 0;
		}

		consoleLogger.info("Reviews extracted: {}", reviews.getReviews().size());

		createCollection(Review.class);

		consoleLogger.info("Inserting reviews into mongodb");

		for (Review review : reviews.getReviews())
		{
			mongoOperations.insert(review);
		}

		return reviews.getReviews().size();
	}

	void createCollection(Class<?> clazz)
	{
		if (!mongoOperations.collectionExists(clazz))
		{
			mongoOperations.createCollection(clazz);
		}
	}
}
