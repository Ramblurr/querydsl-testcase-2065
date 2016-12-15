package querydslbug.repository;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import querydslbug.model.Customer;

public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long>,
        QueryDslPredicateExecutor<Customer>
{
}
