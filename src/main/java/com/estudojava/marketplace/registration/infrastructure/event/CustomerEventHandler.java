package com.estudojava.marketplace.registration.infrastructure.event;

import com.estudojava.marketplace.common.infrastructure.event.dto.CustomerCreated;
import com.estudojava.marketplace.registration.infrastructure.persistence.entity.Customer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class CustomerEventHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerEventHandler.class);

    private final ApplicationEventPublisher publisher;

    public CustomerEventHandler(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @HandleAfterCreate
    public void afterCreate(Customer customer) {
        LOG.info(String.format("CustomerEventHandler. Criado com sucesso: %s", customer));
        publisher.publishEvent(new CustomerCreated(customer.getId().toString(), customer.getFirstName()));
    }

    @HandleAfterDelete
    public void afterDelete(Customer customer) {
        LOG.info(String.format("CustomerEventHandler. Removido com sucesso: %s", customer));
    }

    @HandleAfterSave
    public void afterSave(Customer customer) {
        LOG.info(String.format("CustomerEventHandler. Registrado com sucesso: %s", customer));
    }
}
