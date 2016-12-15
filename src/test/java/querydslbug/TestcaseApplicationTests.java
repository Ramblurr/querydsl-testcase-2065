package querydslbug;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import querydslbug.model.BillingCustomer;
import querydslbug.model.CRMCustomer;
import querydslbug.model.Customer;
import querydslbug.model.CustomerMatch;
import querydslbug.model.QCRMCustomer;
import querydslbug.repository.BillingCustomerRepository;
import querydslbug.repository.CRMCustomerRepository;
import querydslbug.repository.CustomerMatchRepository;
import querydslbug.repository.CustomerRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestcaseApplicationTests
{

    @Autowired BillingCustomerRepository billingCustomerRepository;
    @Autowired CRMCustomerRepository crmCustomerRepository;
    @Autowired CustomerRepository customerRepository;
    @Autowired CustomerMatchRepository customerMatchRepository;

    private CustomerMatch createCustomers()
    {
        BillingCustomer billingCustomer = new BillingCustomer();
        billingCustomer.setName("Acme Inc.");
        billingCustomer.setTaxId("XYZ1234");

        CRMCustomer crmCustomer = new CRMCustomer();
        crmCustomer.setName("ACME Incorporated");
        crmCustomer.setAccountManager("John Salesguy");

        billingCustomer = billingCustomerRepository.save(billingCustomer);
        crmCustomer = crmCustomerRepository.save(crmCustomer);

        assertThat(customerRepository.count(), equalTo(2L));

        CustomerMatch match = new CustomerMatch();
        match.setFirstCustomer(crmCustomer);
        match.setSecondCustomer(billingCustomer);
        match.setMatchPercent(0.80);

        return customerMatchRepository.save(match);
    }

    @Test
    @Transactional
    @Rollback
    public void testCustomerMatchSearchFirstName()
    {
        // GIVEN 2 Customers and 1 CustomerMatch
        CustomerMatch match = createCustomers();

        String firstNameQuery = "acme";

        // sanity check!
        assertThat(crmCustomerRepository
                        .count(QCRMCustomer.cRMCustomer.name.containsIgnoreCase(firstNameQuery)),
                equalTo(1L));

        // WHEN searching for CustomerMatch with a firstCustomer.name contains 'acme'
        Page<CustomerMatch> page = customerMatchRepository
                .searchFirstCustomerName(new PageRequest(0, 10), firstNameQuery);

        // THEN 1 result should be returned
        assertThat(page.getTotalElements(), equalTo(1L));
        assertThat(page.getContent().get(0).getId(), equalTo(match.getId()));
    }

    @Test
    @Transactional
    @Rollback
    public void testCustomerMatchSearchAccountManager()
    {

        // GIVEN 2 Customers and 1 CustomerMatch
        CustomerMatch match = createCustomers();

        String accountManagerQuery = "sales";

        // sanity check!
        assertThat(crmCustomerRepository
                        .count(QCRMCustomer.cRMCustomer.accountManager
                                .containsIgnoreCase(accountManagerQuery)),
                equalTo(1L));

        // WHEN searching for CustomerMatch with a firstCustomer.accountManager contains 'sales'
        Page<CustomerMatch> page = customerMatchRepository
                .searchFirstCustomerAccountManager(new PageRequest(0, 10), accountManagerQuery);

        // THEN 1 result should be returned
        assertThat(page.getTotalElements(), equalTo(1L));
        CustomerMatch resultMatch = page.getContent().get(0);
        assertThat(resultMatch.getId(), equalTo(match.getId()));

        Customer firstCustomer = resultMatch.getFirstCustomer();
        assertTrue(firstCustomer instanceof CRMCustomer);
    }

}
