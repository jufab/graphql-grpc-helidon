package fr.jufab.grpc.service;

import fr.jufab.database.dto.Address;
import fr.jufab.database.repositories.AddressRepository;
import fr.jufab.grpc.proto.AddressServiceGrpc;
import fr.jufab.grpc.proto.AddressToSave;
import fr.jufab.grpc.proto.QueryAddress;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.helidon.grpc.core.ResponseHelper.complete;

/**
 * @author jufab
 * @version 1.0
 */
public class AddressGrpcService extends AddressServiceGrpc.AddressServiceImplBase {
  final static Logger LOGGER = Logger.getLogger(PersonGrpcService.class.getName());
  AddressRepository addressRepository;

  public AddressGrpcService(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  @Override public void createAddress(AddressToSave request,
      StreamObserver<fr.jufab.grpc.proto.Address> responseObserver) {
    try {
      complete(responseObserver, buildAddress(
          addressRepository.createAddress(Address.builder()
              .street(request.getStreet())
              .zipCode(request.getZipCode())
              .city(request.getCity())
              .build()).get()
          )
      );
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    }
  }

  @Override public void addressById(QueryAddress request,
      StreamObserver<fr.jufab.grpc.proto.Address> responseObserver) {
    try {
      complete(responseObserver,
          buildAddress(addressRepository.getAddressById(request.getId()).get()));
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    }
  }

  private fr.jufab.grpc.proto.Address buildAddress(Address address) {
    return fr.jufab.grpc.proto.Address.newBuilder()
        .setId(address.getId())
        .setStreet(address.getStreet())
        .setZipCode(address.getZipCode())
        .setCity(address.getCity())
        .build();
  }
}
