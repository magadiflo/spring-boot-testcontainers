package dev.magadiflo.springboottestcontainers.service;

import dev.magadiflo.springboottestcontainers.persistence.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService {
    List<Customer> findAllCustomers();

    Optional<Customer> findCustomerById(Long id);

    Optional<Customer> findCustomerByEmail(String email);

    Customer saveCustomer(Customer customer);

    Optional<Customer> updateCustomer(Long id, Customer customer);

    Optional<Boolean> deleteCustomerById(Long id);
}
