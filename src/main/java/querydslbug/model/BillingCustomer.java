package querydslbug.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("billing")
public class BillingCustomer extends Customer
{

    String name;

    String taxId;

    public BillingCustomer()
    {
    }

}
