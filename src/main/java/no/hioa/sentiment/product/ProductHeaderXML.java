package no.hioa.sentiment.product;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reviews")
public class ProductHeaderXML
{
	private List<ProductReviewXML> reviews;

	public ProductHeaderXML()
	{

	}

	public ProductHeaderXML(List<ProductReviewXML> reviews)
	{
		super();
		this.reviews = reviews;
	}

	@XmlElement(name = "review")
	public List<ProductReviewXML> getProductReview()
	{
		return reviews;
	}

	public void setProductReview(List<ProductReviewXML> reviews)
	{
		this.reviews = reviews;
	}

}
