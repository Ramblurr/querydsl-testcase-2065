package querydslbug.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import querydslbug.model.CustomerMatch;

interface CustomerMatchRepositoryCustom
{
    /**
     * BROKEN
     * Search for CustomerMatch entities for firstCustomer.name
     *
     * @param page      paging information
     * @param firstName firstCustomer.name should contain this string
     * @return
     */
    Page<CustomerMatch> searchFirstCustomerName(Pageable page, String firstName);

    /**
     * WORKING
     * Search for CustomerMatch entities for firstCustomer.accountManager
     *
     * @param page                paging information
     * @param firstAccountManager firstCustomer.accountManager should contain this string
     * @return
     */
    Page<CustomerMatch> searchFirstCustomerAccountManager(Pageable page,
            String firstAccountManager);
}
