package querydslbug.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("crm")
public class CRMCustomer extends Customer
{

    String name;

    String accountManager;

    public CRMCustomer()
    {
    }
}
