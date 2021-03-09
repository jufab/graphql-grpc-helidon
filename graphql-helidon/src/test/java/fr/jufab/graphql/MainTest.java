package fr.jufab.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vimalselvam.graphql.GraphqlTemplate;
import io.helidon.common.http.MediaType;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.WebServer;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.json.JsonArray;
import javax.json.JsonObject;
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
  private static WebServer webServer;
  private static WebClient webClient;

  @BeforeAll
  public static void startTheServer() throws Exception {
    webServer = Main.startServerGraphQl();

    long timeout = 2000; // 2 seconds should be enough to start the server
    long now = System.currentTimeMillis();

    while (!webServer.isRunning()) {
      Thread.sleep(100);
      if ((System.currentTimeMillis() - now) > timeout) {
        fail("Failed to start webserver");
      }
    }

    webClient = WebClient.builder()
        .baseUri("http://localhost:" + webServer.port())
        .addMediaSupport(JsonpSupport.create())
        .build();
  }

  @AfterAll
  public static void stopServer() throws Exception {
    if (webServer != null) {
      webServer.shutdown()
          .toCompletableFuture()
          .get(10, TimeUnit.SECONDS);
    }
  }

  @Test
  public void shouldReturnHealthResource() throws ExecutionException, InterruptedException {
    webClient.get()
        .path("/health")
        .request(JsonObject.class)
        .thenAccept(
            jsonObject -> assertThat(jsonObject.getString("status")).isEqualTo("UP")
        )
        .toCompletableFuture()
        .get();
  }

  @Test
  public void shouldReturnMetricsResource() throws ExecutionException, InterruptedException {
    webClient.get()
        .path("/metrics")
        .accept(MediaType.APPLICATION_JSON)
        .request(JsonObject.class)
        .thenAccept(
            jsonObject -> assertThat(jsonObject).isNotEmpty()
                .containsKey("base")
                .containsKey("vendor")
        )
        .toCompletableFuture()
        .get();
  }

  @Test
  @Order(1)
  public void shouldCreatePersonWithAGraphQL() throws Exception {
    InputStream iStream = Main.class.getClassLoader().getResourceAsStream(
        "requests/createPerson.graphql");
    String graphqlPayload = GraphqlTemplate.parseGraphql(iStream, null);
    webClient.post()
        .path("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .submit(graphqlPayload)
        .thenCompose(webClientResponse -> webClientResponse.content().as(JsonObject.class))
        .thenAccept(content -> {
          assertThat(content).containsKey("data");
          JsonObject data = content.getJsonObject("data");
          assertThat(data.getJsonObject("createPersonWithAddress")).isNotEmpty();
          JsonObject person = data.getJsonObject("createPersonWithAddress");
          assertThat(person).isNotEmpty();
          assertThat(person.getString("id")).isEqualTo("1");
          JsonObject address = person.getJsonObject("address");
          assertThat(address).isNotEmpty();
          assertThat(address.getString("id")).isEqualTo("1");
        })
        .toCompletableFuture()
        .get();
  }

  @Test
  @Order(10)
  public void shouldReturnPersonsGraphQL() throws Exception {
    InputStream iStream = Main.class.getClassLoader().getResourceAsStream(
        "requests/getPersons.graphql");
    String graphqlPayload = GraphqlTemplate.parseGraphql(iStream, null);
    webClient.post()
        .path("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .submit(graphqlPayload)
        .thenCompose(webClientResponse -> webClientResponse.content().as(JsonObject.class))
        .thenAccept(content -> {
          assertThat(content).containsKey("data");
          JsonObject data = content.getJsonObject("data");
          assertThat(data.getJsonArray("persons"))
              .isNotNull().hasSizeGreaterThan(0);
          JsonArray persons = data.getJsonArray("persons");
          List<JsonObject> personsList = persons.getValuesAs(JsonObject.class);
          JsonObject person = personsList.get(0);
          assertThat(person.getString("id")).isEqualTo("1");
        })
        .toCompletableFuture()
        .get();
  }

  @Test
  @Order(11)
  public void shouldReturnPersonByIdGraphQL() throws Exception {
    InputStream iStream = Main.class.getClassLoader().getResourceAsStream(
        "requests/getPersonById.graphql");
    ObjectNode variables = new ObjectMapper().createObjectNode();
    variables.put("id", 1);
    String graphqlPayload = GraphqlTemplate.parseGraphql(iStream, variables);
    webClient.post()
        .path("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .submit(graphqlPayload)
        .thenCompose(webClientResponse -> webClientResponse.content().as(JsonObject.class))
        .thenAccept(content -> {
          assertThat(content).containsKey("data");
          JsonObject data = content.getJsonObject("data");
          assertThat(data.getJsonObject("personById")).isNotEmpty();
          JsonObject person = data.getJsonObject("personById");
          assertThat(person).isNotEmpty();
          assertThat(person.getString("id")).isEqualTo("1");
          JsonObject address = person.getJsonObject("address");
          assertThat(address).isNotEmpty();
          assertThat(address.getString("id")).isEqualTo("1");
        })
        .toCompletableFuture()
        .get();
  }

  @Test
  @Order(12)
  public void shouldReturnPersonByFirstNameGraphQL() throws Exception {
    InputStream iStream = Main.class.getClassLoader().getResourceAsStream(
        "requests/getPersonsByFirstName.graphql");
    ObjectNode variables = new ObjectMapper().createObjectNode();
    variables.put("firstname", "Test");
    String graphqlPayload = GraphqlTemplate.parseGraphql(iStream, variables);
    webClient.post()
        .path("/graphql")
        .accept(MediaType.APPLICATION_JSON)
        .submit(graphqlPayload)
        .thenCompose(webClientResponse -> webClientResponse.content().as(JsonObject.class))
        .thenAccept(content -> {
          assertThat(content).containsKey("data");
          JsonObject data = content.getJsonObject("data");
          assertThat(data.getJsonArray("personsByFirstName"))
              .isNotNull().hasSizeGreaterThan(0);
          JsonArray persons = data.getJsonArray("personsByFirstName");
          List<JsonObject> personsList = persons.getValuesAs(JsonObject.class);
          JsonObject person = personsList.get(0);
          assertThat(person.getString("id")).isEqualTo("1");
        })
        .toCompletableFuture()
        .get();
  }
}
