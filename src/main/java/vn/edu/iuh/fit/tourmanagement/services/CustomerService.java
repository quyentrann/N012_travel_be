package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.Customer;
import vn.edu.iuh.fit.tourmanagement.repositories.CustomerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getListCustomer() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void partialUpdateCustomer(Long id, Map<String, Object> updates) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "fullName" -> customer.setFullName((String) value);
                case "dob" -> customer.setDob(LocalDate.parse((String) value));
                case "address" -> customer.setAddress((String) value);
                case "gender" -> customer.setGender((Boolean) value);
            }
        });

        customerRepository.save(customer);
    }
}
