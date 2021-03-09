package fr.jufab.database.repositories;

import fr.jufab.database.dto.Address;
import io.helidon.common.reactive.Single;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbRow;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * @author jufab
 * @version 1.0
 */
public class AddressRepository {
  final Logger logger = Logger.getLogger(AddressRepository.class.getName());
  DbClient dbClient;

  public AddressRepository(DbClient dbClient) {
    this.dbClient = dbClient;
  }

  public Single<Address> getAddressById(int id) throws ExecutionException, InterruptedException {
    return dbClient.execute(
        dbExecute ->
            dbExecute.namedGet("select-address", id)
                .map(row -> {
                  DbRow resultSet = row.get();
                  return new Address(resultSet.column("ID").as(Integer.class),
                      resultSet.column("STREET").as(String.class),
                      resultSet.column("ZIPCODE").as(String.class),
                      resultSet.column("CITY").as(String.class)
                  );
                }));
  }

  public Single<Address> createAddress(Address address) throws ExecutionException, InterruptedException {
    address.setId(getSequence());
    dbClient.execute(dbExecute ->
        dbExecute.namedInsert("insert-address",
            address.getId(), address.getStreet(), address.getZipCode(), address.getCity()))
        .thenAccept(count -> logger.info("insert address count : " + count));
    return Single.just(address);
  }

  int getSequence() throws ExecutionException, InterruptedException {
    return dbClient.execute(dbExecute ->
        dbExecute.get("SELECT NEXTVAL('SEQ_ID_ADDRESS')")
            .map(value -> value.get().column("NEXTVAL('SEQ_ID_ADDRESS')").as(Long.class)))
        .get().intValue();
  }
}
