package querydslbug.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;

import lombok.extern.slf4j.Slf4j;
import querydslbug.model.CustomerMatch;
import querydslbug.model.QBillingCustomer;
import querydslbug.model.QCRMCustomer;
import querydslbug.model.QCustomer;
import querydslbug.model.QCustomerMatch;

@Slf4j
class CustomerMatchRepositoryImpl extends QueryDslRepositorySupport
        implements CustomerMatchRepositoryCustom
{
    private final static QCustomerMatch qCustomerMatch = QCustomerMatch.customerMatch;

    public CustomerMatchRepositoryImpl()
    {
        super(CustomerMatch.class);
    }

    @Override
    public Page<CustomerMatch> searchFirstCustomerName(final Pageable page, final String firstName)
    {
        JPQLQuery<CustomerMatch> searchQuery = this.getQuerydsl().createQuery()
                .select(qCustomerMatch)
                .from(qCustomerMatch);
        BooleanBuilder bb = new BooleanBuilder();

        // ONE - Query crmcustomer
        QCustomer firstSource = qCustomerMatch.firstCustomer;
        QCRMCustomer crm = new QCRMCustomer("crm");
        searchQuery.leftJoin(firstSource, crm._super);

        bb.or(crm.name.containsIgnoreCase(firstName));
        // END ONE

        // TWO - Query billing_customer
        //        try commenting this section out, and discover that
        //        billing_customer is still joined and referenced in the where clause
        QBillingCustomer billing = new QBillingCustomer("billing");
        searchQuery.leftJoin(firstSource, billing._super);

        bb.or(billing.name.containsIgnoreCase(firstName));
        // END TWO

        log.info("ACTUAL PREDICATE: {}", bb.toString());

        searchQuery.where(bb);

        log.info("QUERY: {}", searchQuery.toString());

        JPQLQuery<CustomerMatch> pagedQuery = this.getQuerydsl().applyPagination(page, searchQuery);

        QueryResults<CustomerMatch> queryResults = pagedQuery.fetchResults();

        return new PageImpl<>(queryResults.getResults(), page, queryResults.getTotal());
    }

    @Override
    public Page<CustomerMatch> searchFirstCustomerAccountManager(final Pageable page,
            final String firstAccountManager)
    {
        JPQLQuery<CustomerMatch> searchQuery = this.getQuerydsl().createQuery()
                .select(qCustomerMatch)
                .from(qCustomerMatch);
        BooleanBuilder bb = new BooleanBuilder();

        QCustomer firstSource = qCustomerMatch.firstCustomer;
        QCRMCustomer crm = new QCRMCustomer("crm");
        searchQuery.leftJoin(firstSource, crm._super);
        bb.or(crm.accountManager.containsIgnoreCase(firstAccountManager));

        log.info("ACTUAL PREDICATE: {}", bb.toString());

        searchQuery.where(bb);

        log.info("QUERY: {}", searchQuery.toString());

        JPQLQuery<CustomerMatch> pagedQuery = this.getQuerydsl().applyPagination(page, searchQuery);

        QueryResults<CustomerMatch> queryResults = pagedQuery.fetchResults();

        return new PageImpl<>(queryResults.getResults(), page, queryResults.getTotal());
    }
}
