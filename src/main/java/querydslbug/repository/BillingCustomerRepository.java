package querydslbug.repository;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import querydslbug.model.BillingCustomer;

public interface BillingCustomerRepository
        extends PagingAndSortingRepository<BillingCustomer, Long>,
        QueryDslPredicateExecutor<BillingCustomer>
{
}
