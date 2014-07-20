package no.hioa.sentiment.review;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "reviews")
public class ReviewHeaderXML
{
	private List<Review>	reviews;

	public ReviewHeaderXML()
	{

	}

	public ReviewHeaderXML(List<Review> reviews)
	{
		super();
		this.reviews = reviews;
	}

	@XmlElement(name = "review")
	public List<Review> getReviews()
	{
		return reviews;
	}

	public void setReviews(List<Review> reviews)
	{
		this.reviews = reviews;
	}
}
