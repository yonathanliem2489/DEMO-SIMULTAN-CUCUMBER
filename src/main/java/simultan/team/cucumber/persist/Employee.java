package simultan.team.cucumber.persist;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.io.Serializable;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@ToString
@Document(collection = "employee")
@SuppressWarnings("serial")
public class Employee implements Serializable {

  @Id
  private String id;
  private String fullName;

  @JsonCreator
  @PersistenceConstructor
  @lombok.Builder(builderClassName = "Builder")
  Employee(String id, String fullName) {
    this.id = id;
    this.fullName = fullName;
  }
}
