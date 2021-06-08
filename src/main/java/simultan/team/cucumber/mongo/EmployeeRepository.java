package simultan.team.cucumber.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import simultan.team.cucumber.persist.Employee;


public interface EmployeeRepository extends ReactiveMongoRepository<Employee, String> {

}
