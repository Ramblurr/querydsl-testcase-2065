package querydslbug.repository;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import querydslbug.model.CustomerMatch;

public interface CustomerMatchRepository extends PagingAndSortingRepository<CustomerMatch, Long>,
        QueryDslPredicateExecutor<CustomerMatch>, CustomerMatchRepositoryCustom
{
}
