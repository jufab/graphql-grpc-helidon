package fr.jufab.graphql.datafetcher;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Gender;
import fr.jufab.database.dto.Person;
import fr.jufab.database.repositories.PersonRepository;
import graphql.language.IntValue;
import graphql.language.ObjectValue;
import graphql.schema.DataFetcher;
import java.util.List;

/**
 * @author jufab
 * @version 1.0
 */
public class PersonDataFetcher {
  PersonRepository personRepository;

  public PersonDataFetcher(PersonRepository personRepository) {
    this.personRepository = personRepository;
  }

  public DataFetcher<List<Person>> getPersons() {
    return environment -> personRepository.getPersons().collectList().get();
  }

  public DataFetcher<Person> getPersonById() {
    return environment -> personRepository.getPersonById(Integer.parseInt(environment.getArgument("id").toString())).get();
  }

  public DataFetcher<List<Person>> getPersonsByFirstName() {
    return environment -> personRepository.getPersonsByFirstName(
        environment.getArgument("firstname")).collectList().get();
  }

  public DataFetcher<Person> createPersonWithAddress() {
    return environment -> this.personRepository.createPerson(
        new Person(environment.getArgument("firstname"), environment.getArgument("lastname"),
            environment.getArgument("age"),
            new Address(environment.getArgument("street"), environment.getArgument("zipCode"),
                environment.getArgument("city")),
            Gender.valueOf(environment.getArgument("gender")))).get();
  }
}
