package fr.jufab.graphql.datafetcher;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Person;
import fr.jufab.database.repositories.AddressRepository;
import graphql.schema.DataFetcher;

/**
 * @author jufab
 * @version 1.0
 */
public class AddressDataFetcher {
  AddressRepository addressRepository;

  public AddressDataFetcher(
      AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public DataFetcher<Address> getAddressById() {
    return environment -> this.addressRepository.getAddressById(
        environment.getArgument("id") == null ? ((Person) environment.getSource()).getAddress()
            .getId() : environment.getArgument("id")).get();
  }

  public DataFetcher<Address> createAddress() {
    return environment -> this.addressRepository.createAddress(
        new Address(environment.getArgument("street"), environment.getArgument("zipCode"),
            environment.getArgument("city"))).get();
  }
}
