package querydslbug.repository;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import querydslbug.model.CRMCustomer;

public interface CRMCustomerRepository extends PagingAndSortingRepository<CRMCustomer, Long>,
        QueryDslPredicateExecutor<CRMCustomer>
{
}
