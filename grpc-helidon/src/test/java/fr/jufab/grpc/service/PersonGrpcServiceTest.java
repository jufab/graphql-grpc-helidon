package fr.jufab.grpc.service;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Gender;
import fr.jufab.database.dto.Person;
import fr.jufab.database.repositories.AddressRepository;
import fr.jufab.database.repositories.PersonRepository;
import fr.jufab.grpc.proto.PersonWithAddressToSave;
import fr.jufab.grpc.proto.QueryPerson;
import io.grpc.stub.StreamObserver;
import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonGrpcServiceTest {
  public static final int ID = 1;
  public static final String FIRSTNAME = "firstname";
  public static final String LASTNAME = "lastname";
  public static final int AGE = 30;
  public static final String STREET = "street";
  public static final String ZIP_CODE = "zipCode";
  public static final String CITY = "city";
  @Mock
  AddressRepository addressRepository;
  @Mock
  PersonRepository personRepository;
  @Mock
  StreamObserver<fr.jufab.grpc.proto.Person> personStreamObserver;
  @Mock
  StreamObserver<fr.jufab.grpc.proto.Persons> personsStreamObserver;
  @InjectMocks
  PersonGrpcService personGrpcService;

  Person person;
  Address address;

  @BeforeEach
  void initTest() {
    address = Address.builder().id(ID).street(STREET).zipCode(ZIP_CODE).city(CITY).build();
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
  void shouldReturnAPersonWithId() throws Exception {
    when(personRepository.getPersonById(ID)).thenReturn(Single.just(person));
    when(addressRepository.getAddressById(ID)).thenReturn(Single.just(address));
    QueryPerson queryPerson = QueryPerson.newBuilder().setId(ID).build();

    personGrpcService.personById(queryPerson, personStreamObserver);

    verify(personStreamObserver).onCompleted();

    ArgumentCaptor<fr.jufab.grpc.proto.Person> captor =
        ArgumentCaptor.forClass(fr.jufab.grpc.proto.Person.class);
    verify(personStreamObserver).onNext(captor.capture());

    fr.jufab.grpc.proto.Person personResponse = captor.getValue();
    assertThat(personResponse).isNotNull()
        .hasFieldOrPropertyWithValue("firstname", FIRSTNAME)
        .hasFieldOrPropertyWithValue("lastname", LASTNAME)
        .hasFieldOrPropertyWithValue("age", AGE);
  }

  @Test
  void shouldReturnPersons() throws Exception {
    when(personRepository.getPersons()).thenReturn(Multi.just(person));
    when(addressRepository.getAddressById(ID)).thenReturn(Single.just(address));
    QueryPerson queryPerson = QueryPerson.newBuilder().setId(ID).build();

    personGrpcService.persons(queryPerson, personsStreamObserver);

    verify(personsStreamObserver).onCompleted();

    ArgumentCaptor<fr.jufab.grpc.proto.Persons> captor =
        ArgumentCaptor.forClass(fr.jufab.grpc.proto.Persons.class);
    verify(personsStreamObserver).onNext(captor.capture());

    fr.jufab.grpc.proto.Persons personsResponse = captor.getValue();
    fr.jufab.grpc.proto.Person personTest = fr.jufab.grpc.proto.Person.newBuilder()
        .setId(ID)
        .setAddress(fr.jufab.grpc.proto.Address.newBuilder()
            .setId(ID)
            .setStreet(STREET)
            .setCity(CITY)
            .setZipCode(ZIP_CODE)
            .build())
        .setFirstname(FIRSTNAME)
        .setLastname(LASTNAME)
        .setAge(AGE)
        .setGender(fr.jufab.grpc.proto.Gender.MAN)
        .build();
    assertThat(personsResponse).isNotNull();
    assertThat(personsResponse.getPersonsList()).isNotNull().hasSize(1).contains(personTest);
  }

  @Test
  void shouldReturnAPersonByFirstName() throws Exception {
    when(personRepository.getPersonsByFirstName(FIRSTNAME)).thenReturn(Multi.just(person));
    when(addressRepository.getAddressById(ID)).thenReturn(Single.just(address));
    QueryPerson queryPerson = QueryPerson.newBuilder().setFirstname(FIRSTNAME).build();

    personGrpcService.personsByFirstName(queryPerson, personsStreamObserver);

    verify(personsStreamObserver).onCompleted();

    ArgumentCaptor<fr.jufab.grpc.proto.Persons> captor =
        ArgumentCaptor.forClass(fr.jufab.grpc.proto.Persons.class);
    verify(personsStreamObserver).onNext(captor.capture());

    fr.jufab.grpc.proto.Persons personsResponse = captor.getValue();
    fr.jufab.grpc.proto.Person personTest = fr.jufab.grpc.proto.Person.newBuilder()
        .setId(ID)
        .setAddress(fr.jufab.grpc.proto.Address.newBuilder()
            .setId(ID)
            .setStreet(STREET)
            .setCity(CITY)
            .setZipCode(ZIP_CODE)
            .build())
        .setFirstname(FIRSTNAME)
        .setLastname(LASTNAME)
        .setAge(AGE)
        .setGender(fr.jufab.grpc.proto.Gender.MAN)
        .build();
    assertThat(personsResponse).isNotNull();
    assertThat(personsResponse.getPersonsList()).isNotNull().hasSize(1).contains(personTest);
  }

  @Test
  void shouldSaveAPersonWithAnAddress() throws Exception {
    when(personRepository.createPerson(any(Person.class))).thenReturn(Single.just(person));
    when(addressRepository.getAddressById(ID)).thenReturn(Single.just(address));

    PersonWithAddressToSave personWithAddressToSave = PersonWithAddressToSave.newBuilder()
        .setFirstname(FIRSTNAME)
        .setLastname(LASTNAME)
        .setAge(AGE)
        .setGender(fr.jufab.grpc.proto.Gender.MAN)
        .setStreet(STREET)
        .setZipCode(ZIP_CODE)
        .setCity(CITY)
        .build();

    personGrpcService.createPersonWithAddress(personWithAddressToSave, personStreamObserver);

    verify(personStreamObserver).onCompleted();

    ArgumentCaptor<fr.jufab.grpc.proto.Person> captor =
        ArgumentCaptor.forClass(fr.jufab.grpc.proto.Person.class);
    verify(personStreamObserver).onNext(captor.capture());

    fr.jufab.grpc.proto.Person personsResponse = captor.getValue();
    fr.jufab.grpc.proto.Person personTest = fr.jufab.grpc.proto.Person.newBuilder()
        .setId(ID)
        .setAddress(fr.jufab.grpc.proto.Address.newBuilder()
            .setId(ID)
            .setStreet(STREET)
            .setCity(CITY)
            .setZipCode(ZIP_CODE)
            .build())
        .setFirstname(FIRSTNAME)
        .setLastname(LASTNAME)
        .setAge(AGE)
        .setGender(fr.jufab.grpc.proto.Gender.MAN)
        .build();
    assertThat(personsResponse)
        .isNotNull()
        .isEqualTo(personTest);
  }
}