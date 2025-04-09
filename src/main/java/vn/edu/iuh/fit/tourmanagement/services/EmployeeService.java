package vn.edu.iuh.fit.tourmanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.tourmanagement.models.Employee;
import vn.edu.iuh.fit.tourmanagement.repositories.EmployeeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getListEmployee() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public void partialUpdateEmployee(Long id, Map<String,Object> updates) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        updates.forEach((key, value) -> {
            switch (key) {
                case "fullName" -> employee.setFullName((String) value);
                case "cid" -> employee.setCID((String) value);
                case "dob" -> employee.setDob(LocalDate.parse((String) value));
                case "address" -> employee.setAddress((String) value);
                case "phoneNumber" -> employee.setPhoneNumber((String) value);
                case "gender" -> employee.setGender((Boolean) value);
                case "position" -> employee.setPosition((Boolean) value);
            }
        });
        employeeRepository.save(employee);
    }
}
