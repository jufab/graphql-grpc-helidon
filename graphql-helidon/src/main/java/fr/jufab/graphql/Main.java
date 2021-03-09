package fr.jufab.graphql;

import fr.jufab.database.repositories.AddressRepository;
import fr.jufab.database.repositories.PersonRepository;
import fr.jufab.graphql.datafetcher.AddressDataFetcher;
import fr.jufab.graphql.datafetcher.PersonDataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import io.helidon.common.LogConfig;
import io.helidon.common.configurable.Resource;
import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.graphql.server.GraphQlSupport;
import io.helidon.health.HealthSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static io.helidon.config.ConfigSources.classpath;

/**
 * The application main class.
 */
public final class Main {
  public static final String PERSON_GRAPHQLS = "person.graphqls";
  final static Logger LOGGER = Logger.getLogger(Main.class.getName());

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
  public static void main(final String[] args) {
    startServerGraphQl();
  }

  static WebServer startServerGraphQl() {
    Config config =
        Config.just(classpath("server.yaml"), classpath("db.yaml"), classpath("statements.yaml"));
    LogConfig.configureRuntime();

    Config dbConfig = config.get("db");
    DbClient dbClient = DbClient.builder(dbConfig).build();

    initTable(dbClient);

    HealthSupport health = HealthSupport.builder()
        .addLiveness(DbClientHealthCheck.builder(dbClient).query().build())
        .build();

    WebServer server = WebServer.builder()
        .routing(Routing.builder()
            .register(health)                   // Health at "/health"
            .register(MetricsSupport.create())  // Metrics at "/metrics"
            .register(GraphQlSupport.create(buildSchema(dbClient)))
            .build())
        .config(config.get("server"))
        .build();
    server.start();
    return server;
  }

  private static GraphQLSchema buildSchema(DbClient dbClient) {
    SchemaParser schemaParser = new SchemaParser();
    Resource schemaResource = Resource.create(PERSON_GRAPHQLS);
    TypeDefinitionRegistry typeDefinitionRegistry =
        schemaParser.parse(schemaResource.string(StandardCharsets.UTF_8));
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry,
        buildRuntimeWiring(dbClient));
  }

  private static RuntimeWiring buildRuntimeWiring(DbClient dbClient) {
    AddressRepository addressRepository = new AddressRepository(dbClient);
    AddressDataFetcher addressDataFetcher = new AddressDataFetcher(addressRepository);
    PersonRepository personRepository = new PersonRepository(dbClient);
    PersonDataFetcher personDataFetcher = new PersonDataFetcher(personRepository);
    return RuntimeWiring.newRuntimeWiring()
        .type(TypeRuntimeWiring.newTypeWiring("Query")
            .dataFetcher("persons", personDataFetcher.getPersons()))
        .type(TypeRuntimeWiring.newTypeWiring("Query")
            .dataFetcher("personById", personDataFetcher.getPersonById()))
        .type(TypeRuntimeWiring.newTypeWiring("Query")
            .dataFetcher("personsByFirstName", personDataFetcher.getPersonsByFirstName()))
        .type(TypeRuntimeWiring.newTypeWiring("Person")
            .dataFetcher("address", addressDataFetcher.getAddressById()))
        .type(TypeRuntimeWiring.newTypeWiring("Mutation")
            .dataFetcher("createPersonWithAddress", personDataFetcher.createPersonWithAddress()))
        .type(TypeRuntimeWiring.newTypeWiring("Mutation").dataFetcher("createAddress",
            addressDataFetcher.createAddress()))
        .build();
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
