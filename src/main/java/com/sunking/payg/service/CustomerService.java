package com.sunking.payg.service;

import com.sunking.payg.dto.request.CreateCustomerRequest;
import com.sunking.payg.dto.response.CustomerResponse;
import com.sunking.payg.entity.Customer;
import com.sunking.payg.exception.DuplicateResourceException;
import com.sunking.payg.exception.ResourceNotFoundException;
import com.sunking.payg.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        // Check uniqueness constraints
        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException(
                    "A customer with phone number '" + request.getPhoneNumber() + "' already exists");
        }
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A customer with email '" + request.getEmail() + "' already exists");
        }
        if (request.getNationalId() != null && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException(
                    "A customer with national ID '" + request.getNationalId() + "' already exists");
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .nationalId(request.getNationalId())
                .region(request.getRegion())
                .address(request.getAddress())
                .status(Customer.CustomerStatus.ACTIVE)
                .build();

        customer = customerRepository.save(customer);
        log.info("Created customer: id={}, phone={}", customer.getId(), customer.getPhoneNumber());

        return toResponse(customer);
    }

    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return toResponse(customer);
    }

    public Page<CustomerResponse> listCustomers(String search, Pageable pageable) {
        Page<Customer> customers = (search != null && !search.isBlank())
                ? customerRepository.searchCustomers(search.trim(), pageable)
                : customerRepository.findAll(pageable);
        return customers.map(this::toResponse);
    }

    public Customer getCustomerEntityById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .fullName(c.getFirstName() + " " + c.getLastName())
                .phoneNumber(c.getPhoneNumber())
                .email(c.getEmail())
                .nationalId(c.getNationalId())
                .region(c.getRegion())
                .address(c.getAddress())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
