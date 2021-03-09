package fr.jufab.database.repositories;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Gender;
import fr.jufab.database.dto.Person;
import fr.jufab.database.repositories.PersonRepository;
import io.helidon.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.helidon.config.ConfigSources.classpath;
import static org.assertj.core.api.Assertions.assertThat;

class PersonRepositoryTest {
  public static final String FIRSTNAME = "firstname";
  public static final String LASTNAME = "lastname";
  public static final int AGE = 40;
  static DbClient dbClient;

  PersonRepository personRepository;

  @BeforeAll
  public static void initTestDbClient() {
    Config config = Config.just(classpath("db-test.yaml"),classpath("statements.yaml"));
    LogConfig.configureRuntime();
    Config dbConfig = config.get("db");
    dbClient = DbClient.builder(dbConfig).build();
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-address"))
        .thenAccept(value -> System.out.println("CREATE TABLE ADDRESS OK"));
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-sequence-address"))
        .thenAccept(value -> System.out.println("CREATE SEQUENCE ADDRESS OK"));
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-person"))
        .thenAccept(value -> System.out.println("CREATE TABLE PERSON OK"));
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-sequence-person"))
        .thenAccept(value -> System.out.println("CREATE SEQUENCE PERSON OK"));
  }

  @BeforeEach
  void initTest() {
    personRepository = new PersonRepository(dbClient);
  }

  @Test
  void shouldCreateAPerson() throws ExecutionException, InterruptedException {
    Person personResult = createPerson();
    Thread.sleep(100);
    assertThat(personResult).isNotNull().hasFieldOrProperty("id");
    assertThat(personResult.getAddress()).isNotNull().hasFieldOrProperty("id");
  }

  private Person createPerson() throws ExecutionException, InterruptedException {
    Address address = new Address("street", "zipCode", "city");
    Person person = new Person(FIRSTNAME, LASTNAME, AGE, address, Gender.MAN);
    return personRepository.createPerson(person).get();
  }

  @Nested
  class nestedPerson {

    @Test
    void shouldGetAPersonById() throws ExecutionException, InterruptedException {
      Person person = personRepository.getPersonById(1).get();
      assertThat(person).isNotNull().hasFieldOrPropertyWithValue("lastname", LASTNAME);
    }

    @Test
    void shouldGetPersons() throws ExecutionException, InterruptedException {
      createPerson();
      List<Person> persons = personRepository.getPersons().collectList().get();
      assertThat(persons).isNotNull()
          .anySatisfy(person -> assertThat(person.getLastname()).isEqualTo(LASTNAME));
    }

    @Test
    void shouldGetPersonsByName() throws ExecutionException, InterruptedException {
      List<Person> persons = personRepository.getPersonsByFirstName(FIRSTNAME).collectList().get();
      assertThat(persons).isNotNull()
          .anySatisfy(person -> assertThat(person.getFirstname()).isEqualTo(FIRSTNAME));
    }

    @Test
    void shouldNotGetPersonsByName() throws ExecutionException, InterruptedException {
      List<Person> persons = personRepository.getPersonsByFirstName("TEST").collectList().get();
      assertThat(persons).isNotNull().hasSize(0);
    }
  }
}