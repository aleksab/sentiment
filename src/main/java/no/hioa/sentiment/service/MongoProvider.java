package no.hioa.sentiment.service;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoProvider
{
	public static MongoOperations getMongoProvider(String host, String dbName, String username, String password) throws UnknownHostException
	{
		List<MongoCredential> credentialsList = new LinkedList<>();
		credentialsList.add(MongoCredential.createMongoCRCredential(username, "admin", password.toCharArray()));
		MongoClient client = new MongoClient(new ServerAddress(host), credentialsList);
		return new MongoTemplate(new SimpleMongoDbFactory(client, dbName));
	}
	
	public static MongoOperations getMongoProvider(String host, Corpus corpus) throws UnknownHostException
	{
		return new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(new ServerAddress(host)), corpus.getDbName()));
	}

	public static MongoOperations getMongoProvider(Corpus corpus) throws UnknownHostException
	{
		return new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), corpus.getDbName()));
	}
}
