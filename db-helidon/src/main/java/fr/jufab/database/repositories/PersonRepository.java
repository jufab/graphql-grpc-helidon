package fr.jufab.database.repositories;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Gender;
import fr.jufab.database.dto.Person;
import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * @author jufab
 * @version 1.0
 */
public class PersonRepository {
  final Logger logger = Logger.getLogger(AddressRepository.class.getName());
  DbClient dbClient;

  public PersonRepository(DbClient dbClient) {
    this.dbClient = dbClient;
  }

  public Multi<Person> getPersons() throws ExecutionException, InterruptedException {
    return dbClient.execute(dbExecute -> dbExecute.namedQuery("select-all-person"))
        .map(result -> new Person(result.column("ID").as(Integer.class),
            result.column("FIRSTNAME").as(String.class),
            result.column("LASTNAME").as(String.class),
            result.column("AGE").as(Integer.class),
            new Address(result.column("ADDRESS_ID").as(Integer.class)),
            Gender.valueOf(result.column("GENDER").as(String.class))));
  }

  public Single<Person> getPersonById(int id) throws ExecutionException, InterruptedException {
    return dbClient.execute(dbExecute -> dbExecute.namedGet("select-person", id)
        .map(value -> {
          DbRow resultSet = value.get();
          return new Person(resultSet.column("ID").as(Integer.class),
              resultSet.column("FIRSTNAME").as(String.class),
              resultSet.column("LASTNAME").as(String.class),
              resultSet.column("AGE").as(Integer.class),
              new Address(resultSet.column("ADDRESS_ID").as(Integer.class)),
              Gender.valueOf(resultSet.column("GENDER").as(String.class))
          );
        }));
  }

  public Multi<Person> getPersonsByFirstName(String firstName) {
    return dbClient.execute(
        dbExecute -> dbExecute.namedQuery("select-all-person-firstname", firstName))
        .map(result -> new Person(result.column("ID").as(Integer.class),
            result.column("FIRSTNAME").as(String.class),
            result.column("LASTNAME").as(String.class),
            result.column("AGE").as(Integer.class),
            new Address(result.column("ADDRESS_ID").as(Integer.class)),
            Gender.valueOf(result.column("GENDER").as(String.class))));
  }

  public Single<Person> createPerson(Person person)
      throws ExecutionException, InterruptedException {
    AddressRepository addressRepository = new AddressRepository(this.dbClient);
    Address address = addressRepository.createAddress(person.getAddress()).get();
    person.setAddress(address);
    person.setId(getSequence());
    dbClient.execute(dbExecute ->
        dbExecute.namedInsert("insert-person",
            person.getId(), person.getFirstname(), person.getLastname(), person.getAge(),
            person.getGender().name(), person.getAddress().getId()))
        .thenAccept(count -> logger.info("insert person OK"));
    return Single.just(person);
  }

  int getSequence() throws ExecutionException, InterruptedException {
    return dbClient.execute(dbExecute ->
        dbExecute.get("SELECT NEXTVAL('SEQ_ID_PERSON')")
            .map(value -> value.get().column("NEXTVAL('SEQ_ID_PERSON')").as(Long.class)))
        .get().intValue();
  }
}
