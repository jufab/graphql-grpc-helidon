package fr.jufab.database.repositories;

import fr.jufab.database.dto.Address;
import io.helidon.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.helidon.config.ConfigSources.classpath;
import static org.assertj.core.api.Assertions.assertThat;

class AddressRepositoryTest {

  private static DbClient dbClient;

  AddressRepository addressRepository;

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
  }

  @BeforeEach
  void initTest() {
    addressRepository = new AddressRepository(dbClient);
  }

  @Test
  void shouldCreateAnAddress() throws ExecutionException, InterruptedException {
    Address addressResult = createAddress();
    assertThat(addressResult).isNotNull().hasFieldOrProperty("id");
  }

  private Address createAddress() throws ExecutionException, InterruptedException {
    Address address = new Address("street", "zipCode", "city");
    Address addressResult = addressRepository.createAddress(address).get();
    return addressResult;
  }

  @Test
  void shouldGetAnAddressById() throws ExecutionException, InterruptedException {
    Address addressCreated = createAddress();
    Thread.sleep(100);
    Address addressFound = addressRepository.getAddressById(addressCreated.getId()).get();
    assertThat(addressFound).isNotNull().hasFieldOrProperty("id");
  }
}