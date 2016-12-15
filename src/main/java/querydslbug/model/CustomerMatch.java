package querydslbug.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a possible match between two customer records
 */
@Entity
@Getter
@Setter
public class CustomerMatch
{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    /**
     * Probability, in percentage, that these customer records match.
     */
    double matchPercent;

    @ManyToOne
    Customer firstCustomer;

    @ManyToOne
    Customer secondCustomer;

    public CustomerMatch()
    {
    }
}
