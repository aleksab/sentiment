package no.hioa.sentiment.filmweb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "movies")
public class MovieHeader
{
	private List<Movie>	movies;

	public MovieHeader()
	{

	}

	public MovieHeader(List<Movie> movies)
	{
		super();
		this.movies = movies;
	}

	@XmlElement(name = "movie")
	public List<Movie> getMovies()
	{
		return movies;
	}

	public void setMovies(List<Movie> movies)
	{
		this.movies = movies;
	}

}
