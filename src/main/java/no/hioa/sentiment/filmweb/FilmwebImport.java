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

import com.mongodb.MongoClient;

public class FilmwebImport
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	MongoOperations				mongoOperations;

	public static void main(String[] args) throws UnknownHostException
	{
		PropertyConfigurator.configure("log4j.properties");

		// new FilmwebImport().insertXmlIntoMongo(new File("C:/Development/workspace juno/Hioa - Crawler/target/result.xml"));
		new FilmwebImport().printStats();
	}

	public FilmwebImport() throws UnknownHostException
	{
		mongoOperations = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), "filmweb"));
	}

	public void insertXmlIntoMongo(File xmlFile)
	{
		MovieHeader movies = null;

		try
		{
			JAXBContext context = JAXBContext.newInstance(MovieHeader.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			movies = (MovieHeader) unmarshaller.unmarshal(xmlFile);
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
		}

		consoleLogger.info("Movies extracted: {}", movies.getMovies().size());

		if (mongoOperations.collectionExists(Movie.class))
		{
			mongoOperations.dropCollection(Movie.class);
		}

		// mongoOperations.createCollection(Movie.class);

		consoleLogger.info("Inserting movies into mongodb");
		for (Movie movie : movies.getMovies())
		{
			mongoOperations.insert(movie);
		}

		List<Movie> result = mongoOperations.findAll(Movie.class);
		consoleLogger.info("Number of movies inserted into mongodb: {}", result.size());
	}

	public void printStats()
	{
		List<Movie> movies = mongoOperations.findAll(Movie.class);
		consoleLogger.info("Number of movies in mongodb: {}", movies.size());

		long reviews = 0;
		for (Movie movie : movies)
			reviews += movie.getReviews().size();
		consoleLogger.info("Number of reviews in mongodb: {}", reviews);
	}
}
