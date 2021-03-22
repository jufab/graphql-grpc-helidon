package fr.jufab.grpc.service;

import fr.jufab.database.dto.Address;
import fr.jufab.database.dto.Gender;
import fr.jufab.database.dto.Person;
import fr.jufab.database.repositories.AddressRepository;
import fr.jufab.database.repositories.PersonRepository;
import fr.jufab.grpc.proto.PersonServiceGrpc;
import fr.jufab.grpc.proto.PersonToSave;
import fr.jufab.grpc.proto.PersonWithAddressToSave;
import fr.jufab.grpc.proto.QueryPerson;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.helidon.grpc.core.ResponseHelper.complete;

/**
 * @author jufab
 * @version 1.0
 */
public class PersonGrpcService extends PersonServiceGrpc.PersonServiceImplBase {
  final static Logger LOGGER = Logger.getLogger(PersonGrpcService.class.getName());

  PersonRepository personRepository;
  AddressRepository addressRepository;

  public PersonGrpcService(PersonRepository personRepository,
      AddressRepository addressRepository) {
    this.personRepository = personRepository;
    this.addressRepository = addressRepository;
  }

  @Override public void persons(QueryPerson request,
      StreamObserver<fr.jufab.grpc.proto.Persons> responseObserver) {
    try {
      complete(responseObserver, buildPersonsGrpc(personRepository.getPersons()
          .collectList()
          .get()));
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    }
  }

  @Override public void personById(QueryPerson request,
      StreamObserver<fr.jufab.grpc.proto.Person> responseObserver) {
    try {
      complete(responseObserver,
          buildPersonGrpc(personRepository.getPersonById(request.getId()).get()));
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    }
  }

  @Override public void personsByFirstName(QueryPerson request,
      StreamObserver<fr.jufab.grpc.proto.Persons> responseObserver) {
    try {
      complete(responseObserver,
          buildPersonsGrpc(
              personRepository.getPersonsByFirstName(request.getFirstname()).collectList().get()));
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    }
  }

  @Override public void createPersonWithAddress(PersonWithAddressToSave request,
      StreamObserver<fr.jufab.grpc.proto.Person> responseObserver) {
    try {
      complete(responseObserver, buildPersonGrpc(personRepository.createPerson(
          new Person(request.getFirstname(), request.getLastname(), request.getAge(),
              new Address(request.getStreet(), request.getZipCode(), request.getCity()),
              Gender.valueOf(request.getGender().name()))).get()));
    } catch (InterruptedException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    } catch (ExecutionException e) {
      LOGGER.log(Level.SEVERE, "Error", e);
    }
  }

  private fr.jufab.grpc.proto.Persons buildPersonsGrpc(List<Person> personList) {
    return fr.jufab.grpc.proto.Persons.newBuilder().addAllPersons(
        personList.stream()
            .map(this::buildPersonGrpc)
            .collect(Collectors.toList())).build();
  }

  private fr.jufab.grpc.proto.Person buildPersonGrpc(Person person) {
    return fr.jufab.grpc.proto.Person.newBuilder()
        .setId(person.getId())
        .setAddress(buildAddressGrpc(person.getAddress()))
        .setFirstname(person.getFirstname())
        .setLastname(person.getLastname())
        .setAge(person.getAge())
        .setGender(fr.jufab.grpc.proto.Gender.valueOf(person.getGender().name()))
        .build();
  }

  private fr.jufab.grpc.proto.Address buildAddressGrpc(Address address) {
    if (address.getId() > 0) {
      try {
        address = addressRepository.getAddressById(address.getId()).get();
      } catch (InterruptedException e) {
        LOGGER.log(Level.SEVERE, "Error", e);
      } catch (ExecutionException e) {
        LOGGER.log(Level.SEVERE, "Error", e);
      }
    }
    return fr.jufab.grpc.proto.Address.newBuilder()
        .setId(address.getId())
        .setStreet(address.getStreet())
        .setZipCode(address.getZipCode())
        .setCity(address.getCity())
        .build();
  }
}
