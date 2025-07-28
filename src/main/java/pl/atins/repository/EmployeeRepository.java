package pl.atins.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.atins.domain.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByJobTitle(String jobTitle);

    List<Employee> findBySupervisorId(Long supervisorId);

    @Query("SELECT e FROM Employee e WHERE e.firstName LIKE %:name% OR e.lastName LIKE %:name%")
    List<Employee> findByNameContaining(@Param("name") String name);

    @Query("SELECT e FROM Employee e JOIN e.departments d WHERE d.id = :departmentId")
    List<Employee> findByDepartmentId(@Param("departmentId") Long departmentId);

}