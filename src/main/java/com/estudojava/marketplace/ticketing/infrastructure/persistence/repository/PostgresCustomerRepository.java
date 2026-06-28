package com.estudojava.marketplace.ticketing.infrastructure.persistence.repository;

import com.estudojava.marketplace.ticketing.domain.Customer;
import com.estudojava.marketplace.ticketing.domain.CustomerRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCustomerRepository implements CustomerRepository {
    private final CustomerCrudRepository customerCrudRepository;

    public PostgresCustomerRepository(CustomerCrudRepository customerCrudRepository) {
        this.customerCrudRepository = customerCrudRepository;
    }

    @Override
    public void save(Customer customer) {
        var entity = new com.estudojava.marketplace.ticketing.infrastructure.persistence.entity.Customer(
                customer.getId(),
                customer.getCorrelationId().id(),
                customer.getName()
        );
        customerCrudRepository.save(entity);
    }

    /*@Override
    public List<Customer> findAll() {
        var iterable =  customerCrudRepository.findAll();
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(PostgresCustomerRepository::mapper)
                .toList();
    }


    private static com.estudojava.marketplace.ticketing.infrastructure.persistence.entity.Customer mapper(Customer customer) {
        var entity = new com.estudojava.marketplace.ticketing.infrastructure.persistence.entity.Customer();

        entity.setId(customer.getId().id());
        entity.setFirstName(customer.getName());
        entity.setEmail(customer.getEmail());

        return entity;
    }

    private static Customer mapper(com.estudojava.marketplace.ticketing.infrastructure.persistence.entity.Customer entity) {

        String fullName = Optional.ofNullable(entity.getLastName())
                .map(lastName -> entity.getFirstName() + " " + lastName)
                .orElseGet(entity::getFirstName);

        return new Customer(new CustomerId(entity.getId()), fullName, entity.getEmail());
    }*/
}
