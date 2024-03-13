package dev.magadiflo.springboottestcontainers.service.impl;

import dev.magadiflo.springboottestcontainers.persistence.entity.Customer;
import dev.magadiflo.springboottestcontainers.persistence.repository.CustomerRepository;
import dev.magadiflo.springboottestcontainers.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAllCustomers() {
        return this.customerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerById(Long id) {
        return this.customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findCustomerByEmail(String email) {
        return this.customerRepository.findCustomerByEmail(email);
    }

    @Override
    @Transactional
    public Customer saveCustomer(Customer customer) {
        return this.customerRepository.save(customer);
    }

    @Override
    @Transactional
    public Optional<Customer> updateCustomer(Long id, Customer customer) {
        return this.customerRepository.findById(id)
                .map(customerDB -> {
                    customerDB.setName(customer.getName());
                    customerDB.setEmail(customer.getEmail());
                    return customerDB;
                })
                .map(this.customerRepository::save);
    }

    @Override
    @Transactional
    public Optional<Boolean> deleteCustomerById(Long id) {
        return this.customerRepository.findById(id)
                .map(customerDB -> {
                    this.customerRepository.deleteById(id);
                    return true;
                });
    }
}