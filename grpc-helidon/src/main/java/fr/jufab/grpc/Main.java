package fr.jufab.grpc;

import fr.jufab.database.repositories.AddressRepository;
import fr.jufab.database.repositories.PersonRepository;
import fr.jufab.grpc.service.AddressGrpcService;
import fr.jufab.grpc.service.PersonGrpcService;
import io.helidon.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.grpc.server.GrpcRouting;
import io.helidon.grpc.server.GrpcServer;
import io.helidon.grpc.server.GrpcServerConfiguration;
import io.helidon.health.HealthSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static io.helidon.config.ConfigSources.classpath;

/**
 * The application main class.
 */
public final class Main {
  final static Logger LOGGER = Logger.getLogger(AddressRepository.class.getName());

  /**
   * Cannot be instantiated.
   */
  private Main() {
  }

  /**
   * Application main entry point.
   *
   * @param args command line arguments.
   */
  public static void main(final String[] args)
      throws InterruptedException, ExecutionException, TimeoutException {
    startServer();
  }

  /**
   * Start the server.
   *
   * @return the created {@link GrpcServer} instance
   */
  static GrpcServer startServer()
      throws InterruptedException, ExecutionException, TimeoutException {
    Config config =
        Config.just(classpath("server.yaml"), classpath("db.yaml"), classpath("statements.yaml"));
    LogConfig.configureRuntime();

    Config dbConfig = config.get("db");
    DbClient dbClient = DbClient.builder(dbConfig).build();

    initTable(dbClient);

    GrpcServer grpcServer = GrpcServer
        .create(GrpcServerConfiguration.create(config.get("grpcserver")), GrpcRouting.builder()
            .register(buildPersonServiceGrpc(dbClient))
            .register(buildAddressServiceGrpc(dbClient))
            .build())
        .start()
        .toCompletableFuture()
        .get(10, TimeUnit.SECONDS);

    return grpcServer;
  }

  static PersonGrpcService buildPersonServiceGrpc(DbClient dbClient) {
    PersonRepository personRepository = new PersonRepository(dbClient);
    AddressRepository addressRepository = new AddressRepository(dbClient);
    return new PersonGrpcService(personRepository, addressRepository);
  }

  static AddressGrpcService buildAddressServiceGrpc(DbClient dbClient) {
    AddressRepository addressRepository = new AddressRepository(dbClient);
    return new AddressGrpcService(addressRepository);
  }

  /**
   * Init Table Schema
   *
   * @param dbClient
   */
  static void initTable(DbClient dbClient) {
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-address"))
        .thenAccept(value -> LOGGER.info("CREATE TABLE ADDRESS OK"));
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-sequence-address"))
        .thenAccept(value -> LOGGER.info("CREATE SEQUENCE ADDRESS OK"));
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-person"))
        .thenAccept(value -> LOGGER.info("CREATE TABLE PERSON OK"));
    dbClient.execute(dbExecute -> dbExecute.namedDml("create-sequence-person"))
        .thenAccept(value -> LOGGER.info("CREATE SEQUENCE PERSON OK"));
  }
}
