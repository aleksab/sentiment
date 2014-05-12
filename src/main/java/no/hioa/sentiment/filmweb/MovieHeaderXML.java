package no.hioa.sentiment.filmweb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "movies")
public class MovieHeaderXML
{
	private List<MovieXML>	movies;

	public MovieHeaderXML()
	{

	}

	public MovieHeaderXML(List<MovieXML> movies)
	{
		super();
		this.movies = movies;
	}

	@XmlElement(name = "movie")
	public List<MovieXML> getMovies()
	{
		return movies;
	}

	public void setMovies(List<MovieXML> movies)
	{
		this.movies = movies;
	}

}
