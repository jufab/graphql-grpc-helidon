package fr.jufab.graphql.datafetcher;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Gender;
import fr.jufab.database.dto.Person;
import fr.jufab.database.repositories.PersonRepository;
import graphql.schema.DataFetchingEnvironment;
import io.helidon.common.reactive.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonDataFetcherTest {

  public static final int ID = 1;
  public static final String FIRSTNAME = "firstname";
  public static final String LASTNAME = "lastname";
  public static final int AGE = 30;
  @Mock PersonRepository personRepository;
  @Spy DataFetchingEnvironment environment;
  @InjectMocks PersonDataFetcher personDataFetcher;

  Person person;

  @BeforeEach
  void initTest() {
    Address address = Address.builder().build();
    person = Person.builder()
        .id(ID)
        .firstname(FIRSTNAME)
        .lastname(LASTNAME)
        .age(AGE)
        .gender(Gender.MAN)
        .address(address)
        .build();
  }

  @Test
  void shouldReturnPersonFromPersonDataFetcher() throws Exception {
    when(environment.getArgument("id")).thenReturn(ID);
    when(personRepository.getPersonById(ID)).thenReturn(Single.just(person));
    Person personResult = personDataFetcher.getPersonById().get(environment);
    assertThat(personResult).isNotNull()
        .hasFieldOrPropertyWithValue("firstname", FIRSTNAME)
        .hasFieldOrPropertyWithValue("lastname", LASTNAME)
        .hasFieldOrPropertyWithValue("age", AGE)
        .hasFieldOrPropertyWithValue("gender", Gender.MAN);
  }
}