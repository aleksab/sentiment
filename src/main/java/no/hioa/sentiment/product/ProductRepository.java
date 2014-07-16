package no.hioa.sentiment.product;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProductRepository extends PagingAndSortingRepository<ProductReview, Long>
{

}
