package no.hioa.sentiment.filmweb;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.persistence.internal.nosql.adapters.mongo.MongoConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;

public class PopulateData
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		logger.info("Starting");

		Movie movie = new Movie("Test", "action");
		MovieRating r1 = new MovieRating(5);
		MovieRating r2 = new MovieRating(3);
		movie.getOrderLines().add(r1);
		movie.getOrderLines().add(r2);

		new PopulateData().insertMovie(movie);
	}

	public void insertMovie(Movie movie)
	{
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("mongo");
		EntityManager em = factory.createEntityManager();
		// First clear old database.
		em.getTransaction().begin();
		DB db = ((MongoConnection) em.unwrap(javax.resource.cci.Connection.class)).getDB();
		db.dropDatabase();
		em.getTransaction().commit();

		em.getTransaction().begin();
		em.persist(movie);
		em.getTransaction().commit();
		String uid = movie.getId();
		em.close();
		em = factory.createEntityManager();

		Movie order = em.find(Movie.class, uid);
		System.out.println("Found movie:" + order + " by its oid: " + uid);
		em.close();
	}
}
