package no.hioa.sentiment.forum;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ForumRepository extends PagingAndSortingRepository<Post, Long>
{

}
