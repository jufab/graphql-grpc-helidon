package fr.jufab.grpc;

import fr.jufab.grpc.proto.Gender;
import fr.jufab.grpc.proto.Person;
import fr.jufab.grpc.proto.PersonServiceGrpc;
import fr.jufab.grpc.proto.PersonWithAddressToSave;
import fr.jufab.grpc.proto.Persons;
import fr.jufab.grpc.proto.QueryPerson;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.helidon.grpc.client.ClientServiceDescriptor;
import io.helidon.grpc.client.GrpcServiceClient;
import io.helidon.grpc.server.GrpcServer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainTest {

  public static final String LASTNAME = "Lastname";
  public static final String FIRSTNAME = "Firstname";
  public static final int AGE = 30;
  public static final Gender GENDER = Gender.MAN;
  public static final String STREET = "Street";
  public static final String ZIP_CODE = "ZipCode";
  public static final String CITY = "City";
  public static final int ID = 1;
  private static GrpcServer grpcServer;
  private static GrpcServiceClient personGrpcClient;

  @BeforeAll
  public static void startTheServer() throws Exception {
    grpcServer = Main.startServer();

    long timeout = 2000; // 2 seconds should be enough to start the server
    long now = System.currentTimeMillis();

    while (!grpcServer.isRunning()) {
      Thread.sleep(100);
      if ((System.currentTimeMillis() - now) > timeout) {
        fail("Failed to start grpcServer");
      }
    }

    ClientServiceDescriptor personClientServiceDescriptor = ClientServiceDescriptor
        .builder(PersonServiceGrpc.getServiceDescriptor())
        .build();

    Channel channel = ManagedChannelBuilder.forAddress("localhost", 3333)           // (9)
        .usePlaintext().build();

    personGrpcClient = GrpcServiceClient.create(channel, personClientServiceDescriptor);
  }

  @AfterAll
  public static void stopServer() throws Exception {
    if (grpcServer != null) {
      grpcServer.shutdown()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);
    }
  }

  @Test
  @Order(1)
  void shouldCreateAPersonWithAnAddress() {
    Person person = personGrpcClient.blockingUnary(
        PersonServiceGrpc.METHOD_CREATE_PERSON_WITH_ADDRESS.getBareMethodName(),
        PersonWithAddressToSave.newBuilder()
            .setLastname(LASTNAME)
            .setFirstname(FIRSTNAME)
            .setAge(AGE)
            .setGender(GENDER)
            .setStreet(STREET)
            .setZipCode(ZIP_CODE)
            .setCity(CITY)
            .build());
    assertThat(person).isNotNull()
        .hasFieldOrPropertyWithValue("lastname", LASTNAME)
        .hasFieldOrPropertyWithValue("firstname", FIRSTNAME)
        .hasFieldOrPropertyWithValue("age", AGE)
        .hasFieldOrPropertyWithValue("gender", GENDER);
    assertThat(person.getAddress()).isNotNull()
        .hasFieldOrPropertyWithValue("street", STREET)
        .hasFieldOrPropertyWithValue("zipCode", ZIP_CODE)
        .hasFieldOrPropertyWithValue("city", CITY);
  }

  @Test
  @Order(2)
  void shouldReturnPersons() throws ExecutionException, InterruptedException {
    QueryPerson queryPerson = QueryPerson.newBuilder().build();
    Persons persons =
        personGrpcClient.blockingUnary(PersonServiceGrpc.METHOD_PERSONS.getBareMethodName(),
            queryPerson);
    assertThat(persons).isNotNull();
    assertThat(persons.getPersonsList()).isNotNull()
        .isNotEmpty()
        .hasSize(1);
    assertThat(persons.getPersons(0)).isNotNull()
        .hasFieldOrPropertyWithValue("lastname", LASTNAME)
        .hasFieldOrPropertyWithValue("firstname", FIRSTNAME)
        .hasFieldOrPropertyWithValue("age", AGE)
        .hasFieldOrPropertyWithValue("gender", GENDER);
  }

  @Test
  @Order(3)
  void shouldReturnAPersonById() {
    QueryPerson queryPerson = QueryPerson.newBuilder().setId(1).build();
    Person person =
        personGrpcClient.blockingUnary(PersonServiceGrpc.METHOD_PERSON_BY_ID.getBareMethodName(),
            queryPerson);
    assertThat(person).isNotNull()
        .hasFieldOrPropertyWithValue("lastname", LASTNAME)
        .hasFieldOrPropertyWithValue("firstname", FIRSTNAME)
        .hasFieldOrPropertyWithValue("age", AGE)
        .hasFieldOrPropertyWithValue("gender", GENDER);
    assertThat(person.getAddress()).isNotNull()
        .hasFieldOrPropertyWithValue("street", STREET)
        .hasFieldOrPropertyWithValue("zipCode", ZIP_CODE)
        .hasFieldOrPropertyWithValue("city", CITY);
  }

  @Test
  @Order(4)
  void shouldReturnAPersonByFirstname() {
    QueryPerson queryPerson = QueryPerson.newBuilder().setFirstname(FIRSTNAME).build();
    Persons persons =
        personGrpcClient.blockingUnary(
            PersonServiceGrpc.METHOD_PERSONS_BY_FIRST_NAME.getBareMethodName(),
            queryPerson);
    assertThat(persons).isNotNull();
    assertThat(persons.getPersonsList()).isNotNull()
        .isNotEmpty()
        .hasSize(1);
    assertThat(persons.getPersons(0)).isNotNull()
        .hasFieldOrPropertyWithValue("lastname", LASTNAME)
        .hasFieldOrPropertyWithValue("firstname", FIRSTNAME)
        .hasFieldOrPropertyWithValue("age", AGE)
        .hasFieldOrPropertyWithValue("gender", GENDER);
  }
}
