package no.hioa.sentiment.filmweb;

import java.io.File;
import java.net.UnknownHostException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.mongodb.MongoClient;

public class FilmwebImport
{
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-db", description = "Mongo database name")
	private String dbName = "filmweb";

	@Parameter(names = "-xml", description = "Path to xml file")
	private String xmlFile;

	@Parameter(names = "-p", description = "Print statistics")
	private boolean printStats = false;

	private MongoOperations mongoOperations;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		new FilmwebImport(args);
	}

	public FilmwebImport(String[] args) throws UnknownHostException
	{
		JCommander commander = new JCommander(this, args);
		mongoOperations = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), dbName));

		if (printStats)
			printStats();
		else if (xmlFile == null)
			commander.usage();
		else
		{
			long result = insertXmlIntoMongo(new File(xmlFile));
			consoleLogger.info("{} movies have been imported", result);
		}
	}

	public void printStats()
	{
		List<Movie> movies = mongoOperations.findAll(Movie.class);
		consoleLogger.info("Number of movies in {} is {}", dbName, movies.size());

		List<Review> reviews = mongoOperations.findAll(Review.class);
		consoleLogger.info("Number of reviews in {} is {}", dbName, reviews.size());
	}

	public long insertXmlIntoMongo(File xmlFile)
	{
		MovieHeaderXML movies = null;

		try
		{
			JAXBContext context = JAXBContext.newInstance(MovieHeaderXML.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			movies = (MovieHeaderXML) unmarshaller.unmarshal(xmlFile);
		} catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
			return 0;
		}

		consoleLogger.info("Movies extracted: {}", movies.getMovies().size());

		createCollection(Movie.class);
		createCollection(Review.class);

		consoleLogger.info("Inserting movies and reviews into mongodb");

		for (MovieXML movie : movies.getMovies())
		{
			Movie internalMovie = new Movie(movie.getLink(), movie.getTitle(), movie.getOriginalTitle());
			mongoOperations.insert(internalMovie);

			for (ReviewXML review : movie.getReviews())
			{
				Review internalReview = new Review(internalMovie.getId(), review.getLink(), review.getRating(), review.getName(),
						review.getContent(), review.getDomain());
				mongoOperations.insert(internalReview);
			}
		}

		return movies.getMovies().size();
	}

	void createCollection(Class<?> clazz)
	{
		if (mongoOperations.collectionExists(clazz))
		{
			mongoOperations.dropCollection(clazz);
		}

		mongoOperations.createCollection(clazz);
	}
}
