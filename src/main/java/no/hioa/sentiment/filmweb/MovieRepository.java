package no.hioa.sentiment.filmweb;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface MovieRepository extends PagingAndSortingRepository<Review, Long>
{

}
