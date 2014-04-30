package no.hioa.sentiment.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.eclipse.persistence.nosql.annotations.DataFormatType;
import org.eclipse.persistence.nosql.annotations.Field;
import org.eclipse.persistence.nosql.annotations.NoSql;

@Entity
@NoSql(dataFormat=DataFormatType.MAPPED)
public class Movie
{
	@Id
    @GeneratedValue
    @Field(name="_id")
    private String id;

    @Version
    private long version;
    
    @Basic
    private String rawReviewText;
    
    @Basic
    private String genere;
    
    @ElementCollection
    private List<MovieRating> orderLines = new ArrayList<MovieRating>();
    
    
}
