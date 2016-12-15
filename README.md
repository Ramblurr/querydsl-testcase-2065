# Test case for QueryDSL [#2065](https://github.com/querydsl/querydsl/issues/2065)

## Summary

Incorrect WHERE clause is generated for child-objects in an inheritance hierarchy *that share a field of the same name*

Included is a test case which demonstrates this bug.

## Detail

I encountered this bug when trying to workaround #2055.

Given a this entity hierarchy:

![inheritance-diagram](https://cloud.githubusercontent.com/assets/14830/21220601/f56006b2-c2b9-11e6-9f6b-8c05e94e10b7.png)

* `Customer` - base type
  * using `@Inheritance(strategy = InheritanceType.JOINED)`
* `CRMCustomer` & `BillingCustomer` - implementations of Customer 
* `CustomerMatch` - Represents a possible "match" between two `Customer` entities (of any specific type!)

**Note**: The two customer implementations each have 2 fields, one of which happens to be named the same, that is the field `name`.

**Goal:** The goal is to query for `CustomerMatch` entities where the `firstSource.name` contains a string.

```java
        String firstName = "acme";
        JPQLQuery<CustomerMatch> searchQuery = this.getQuerydsl().createQuery()
                .select(qCustomerMatch)
                .from(qCustomerMatch);

        BooleanBuilder bb = new BooleanBuilder();

        QCustomer firstSource = qCustomerMatch.firstCustomer;
 
        // ONE - Query crmcustomer
        QCRMCustomer crm = new QCRMCustomer("crm");
        searchQuery.leftJoin(firstSource, crm._super);
        bb.or(crm.name.containsIgnoreCase(firstName));
 
        // TWO - Query billing_customer
        QBillingCustomer billing = new QBillingCustomer("billing");
        searchQuery.leftJoin(firstSource, billing._super);
        bb.or(billing.name.containsIgnoreCase(firstName));

        searchQuery.where(bb);
        searchQuery.fetchResults();
```

Doing a `log.info(searchQuery.toString());` outputs the JQPL:

```sql
select customerMatch
from CustomerMatch customerMatch
  left join treat(customerMatch.firstCustomer as CRMCustomer) as crm
  left join treat(customerMatch.firstCustomer as BillingCustomer) as billing
where lower(crm.name) like ?1 escape '!' or lower(billing.name) like ?1 escape '!'
```

Which becomes the SQL:

```sql
SELECT count(customerma0_.id) AS col_0_0_
FROM customer_match customerma0_
  LEFT OUTER JOIN customer customer1_ ON customerma0_.first_customer_id = customer1_.id
  ††† INNER JOIN billing_customer customer1_1_ ON customer1_.id = customer1_1_.id
  INNER JOIN crmcustomer customer1_2_ ON customer1_.id = customer1_2_.id

  LEFT OUTER JOIN customer customer2_ ON customerma0_.first_customer_id = customer2_.id
  ‡‡‡ INNER JOIN billing_customer customer2_1_ ON customer2_.id = customer2_1_.id
  INNER JOIN crmcustomer customer2_2_ ON customer2_.id = customer2_2_.id
 
WHERE lower(†††customer1_1_.name) LIKE ? ESCAPE '!' OR lower(‡‡‡customer2_1_.name) LIKE ? ESCAPE '!'
```
**The problem**:
Notice the †††  and ‡‡‡ in the sql above. Both of those aliases refer to the `billing_customer` table, *which is totally wrong*. Only the second (‡) should refer to `billing_customer`.

**Even more wrong**, is if we leave out the `// TWO - Query billing_customer` part of the query from above, then we get the JPQL

```sql
select customerMatch
from CustomerMatch customerMatch
  left join treat(customerMatch.firstCustomer as CRMCustomer) as crm
where lower(crm.name) like ?1 escape '!'
```

Which creates:

```sql
SELECT count(customerma0_.id) AS col_0_0_
FROM customer_match customerma0_
  LEFT OUTER JOIN customer customer1_ ON customerma0_.first_customer_id = customer1_.id
  ††LEFT OUTER JOIN billing_customer customer1_1_ ON customer1_.id = customer1_1_.id
  § INNER JOIN crmcustomer customer1_2_ ON customer1_.id = customer1_2_.id
WHERE lower(††customer1_1_.name) LIKE ? ESCAPE '!'
```

The where clause is once again using the alias for `billing_customer`, which was joined despite there being no mention of `BillingCustomer` in the Java or JPQL. And the joined `crmcustomer` alias (§) sits unused.

**Now for the big finale**: This behavior only occurs when I query on the `name` field, a field which both `Customer` implementations *happen* to share. If I query instead for a field that is unique to `CRMCustomer`, that is `account_manager`, **the query works as expected**.

```java
            bb.or(crm.accountManager.containsIgnoreCase("sales"));
```

```sql
select customerMatch
from CustomerMatch customerMatch
  left join treat(customerMatch.firstCustomer as CRMCustomer) as crm
where lower(crm.accountManager) like ?1 escape '!'
```

```sql
SELECT count(customerma0_.id) AS col_0_0_
FROM customer_match customerma0_
  LEFT OUTER JOIN customer customer1_ ON customerma0_.first_customer_id = customer1_.id
  †† LEFT OUTER JOIN billing_customer customer1_1_ ON customer1_.id = customer1_1_.id
  § INNER JOIN crmcustomer customer1_2_ ON customer1_.id = customer1_2_.id
WHERE lower(§customer1_2_.account_manager) LIKE ? ESCAPE '!'
```

This works as expected. There is still an unexpected join on `billing_customer` (††), but this time the WHERE clause references the correct `crmcustomer` alias (§).

### TESTED with Environment:

* querydsl 4.1.4
* spring-boot 1.4.2
* hibernate 5.0.11  AND 5.1
* database drivers:
  * postgresql
  * mysql
  * h2 

## Testcase

This repo contains a minimal spring-boot + QueryDSL application that demonstrates this bug.

There are two test cases  that follow the same situation outlined above:

1. querying `CustomerMatch.firstCustomer.name`
2. querying `CustomerMatch.firstCustomer.accountManager`

The interesting code is in:
* `querydslbug.TestcaseApplicationTests`
* `querydslbug.repository.CustomerMatchRepositoryImpl`


