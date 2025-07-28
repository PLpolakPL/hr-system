package pl.atins.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.atins.domain.Department;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByHeadId(Long headId);

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Department> findByNameContaining(@Param("name") String name);

    @Query("SELECT d FROM Department d WHERE LOWER(d.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<Department> findByLocationContaining(@Param("location") String location);

    @Query("SELECT d FROM Department d WHERE SIZE(d.employees) = 0")
    List<Department> findEmptyDepartments();

    @Query("SELECT d FROM Department d WHERE SIZE(d.employees) > :minSize")
    List<Department> findDepartmentsWithMinEmployees(@Param("minSize") int minSize);
}