package no.hioa.sentiment.filmweb;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;

@Repository
public class HelloMongo
{
	@Autowired
	MongoOperations	mongoOperations;

	public void run()
	{
		if (mongoOperations.collectionExists(Movie.class))
		{
			mongoOperations.dropCollection(Movie.class);
		}

		mongoOperations.createCollection(Movie.class);

		Movie movie = new Movie("Test", "action");
		MovieRating r1 = new MovieRating(5);
		MovieRating r2 = new MovieRating(3);
		movie.getOrderLines().add(r1);
		movie.getOrderLines().add(r2);

		mongoOperations.insert(movie);
		System.out.println("Uid: " + movie.getId());

		List<Movie> results = mongoOperations.findAll(Movie.class);
		System.out.println("Results: " + results);
	}
}
