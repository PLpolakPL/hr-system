package pl.atins.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.atins.domain.Department;
import pl.atins.domain.Employee;
import pl.atins.repository.DepartmentRepository;
import pl.atins.repository.EmployeeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            loadSampleData();
        }
    }

    private void loadSampleData() {
        var itDepartment = new Department();
        itDepartment.setName("Information Technology");
        itDepartment.setDescription("Technology and software development");
        itDepartment.setLocation("Building A, Floor 3");
        itDepartment = departmentRepository.save(itDepartment);

        var hrDepartment = new Department();
        hrDepartment.setName("Human Resources");
        hrDepartment.setDescription("Employee management and recruitment");
        hrDepartment.setLocation("Building B, Floor 1");
        hrDepartment = departmentRepository.save(hrDepartment);

        var ceo = new Employee();
        ceo.setFirstName("John");
        ceo.setLastName("Smith");
        ceo.setEmail("john.smith@company.com");
        ceo.setJobTitle("CEO");
        ceo.setHireDate(LocalDate.of(2020, 1, 15));
        ceo.setSalary(new BigDecimal("150000"));
        ceo.setPhone("+1-555-0001");
        ceo.setOfficeLocation("Executive Suite");
        ceo = employeeRepository.save(ceo);

        var itManager = new Employee();
        itManager.setFirstName("Alice");
        itManager.setLastName("Johnson");
        itManager.setEmail("alice.johnson@company.com");
        itManager.setJobTitle("IT Manager");
        itManager.setHireDate(LocalDate.of(2020, 3, 1));
        itManager.setSalary(new BigDecimal("95000"));
        itManager.setPhone("+1-555-0002");
        itManager.setOfficeLocation("Building A, Floor 3, Room 301");
        itManager.addSupervisor(ceo);
        itManager.addDepartment(itDepartment);
        itManager = employeeRepository.save(itManager);

        itDepartment.setHead(itManager);
        departmentRepository.save(itDepartment);

        var developer1 = new Employee();
        developer1.setFirstName("Bob");
        developer1.setLastName("Wilson");
        developer1.setEmail("bob.wilson@company.com");
        developer1.setJobTitle("Senior Developer");
        developer1.setHireDate(LocalDate.of(2021, 6, 15));
        developer1.setSalary(new BigDecimal("75000"));
        developer1.setPhone("+1-555-0003");
        developer1.setOfficeLocation("Building A, Floor 3, Room 305");
        developer1.addSupervisor(itManager);
        developer1.addDepartment(itDepartment);
        employeeRepository.save(developer1);

        var developer2 = new Employee();
        developer2.setFirstName("Carol");
        developer2.setLastName("Davis");
        developer2.setEmail("carol.davis@company.com");
        developer2.setJobTitle("Junior Developer");
        developer2.setHireDate(LocalDate.of(2022, 9, 1));
        developer2.setSalary(new BigDecimal("55000"));
        developer2.setPhone("+1-555-0004");
        developer2.setOfficeLocation("Building A, Floor 3, Room 306");
        developer2.addSupervisor(itManager);
        developer2.addDepartment(itDepartment);
        employeeRepository.save(developer2);

        var hrManager = new Employee();
        hrManager.setFirstName("Diana");
        hrManager.setLastName("Miller");
        hrManager.setEmail("diana.miller@company.com");
        hrManager.setJobTitle("HR Manager");
        hrManager.setHireDate(LocalDate.of(2020, 5, 1));
        hrManager.setSalary(new BigDecimal("85000"));
        hrManager.setPhone("+1-555-0005");
        hrManager.setOfficeLocation("Building B, Floor 1, Room 101");
        hrManager.addSupervisor(ceo);
        hrManager.addDepartment(hrDepartment);
        hrManager = employeeRepository.save(hrManager);

        hrDepartment.setHead(hrManager);
        departmentRepository.save(hrDepartment);
    }
}