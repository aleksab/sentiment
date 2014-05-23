package no.hioa.sentiment.filmweb;

import java.net.UnknownHostException;

import no.hioa.sentiment.service.Corpus;
import no.hioa.sentiment.service.MongoProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;

public class FilmwebData
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	private MongoOperations		mongoOperations;

	public FilmwebData() throws UnknownHostException
	{
		this.mongoOperations = MongoProvider.getMongoProvider(Corpus.MOVIE_REVIEWS);
	}

	public Review getReview(String id)
	{
		logger.info("Searching for review with id {}", id);
		return mongoOperations.findById(id, Review.class);
	}
}
