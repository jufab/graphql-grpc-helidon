package fr.jufab.grpc.service;

import fr.jufab.database.dto.Address;
import fr.jufab.database.repositories.AddressRepository;
import fr.jufab.grpc.proto.AddressToSave;
import fr.jufab.grpc.proto.QueryAddress;
import io.grpc.stub.StreamObserver;
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
class AddressGrpcServiceTest {
  public static final String STREET = "street";
  public static final String ZIP_CODE = "zipCode";
  public static final String CITY = "city";
  public static final int ID = 1;
  @Mock
  AddressRepository addressRepository;
  @Mock
  StreamObserver<fr.jufab.grpc.proto.Address> addressStreamObserver;
  @InjectMocks
  AddressGrpcService addressGrpcService;

  Address address;

  @BeforeEach
  void initTest() {
    address = Address.builder().id(ID).street(STREET).zipCode(ZIP_CODE).city(CITY).build();
  }

  @Test
  void shouldSaveAnAddress() throws Exception {
    when(addressRepository.createAddress(any(Address.class))).thenReturn(Single.just(address));
    AddressToSave addressToSave =
        AddressToSave.newBuilder().setStreet(STREET).setZipCode(ZIP_CODE).setCity(CITY).build();

    addressGrpcService.createAddress(addressToSave, addressStreamObserver);

    verify(addressStreamObserver).onCompleted();

    ArgumentCaptor<Address> addressArgumentCaptor =
        ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).createAddress(addressArgumentCaptor.capture());

    ArgumentCaptor<fr.jufab.grpc.proto.Address> addressGrpcCaptor =
        ArgumentCaptor.forClass(fr.jufab.grpc.proto.Address.class);
    verify(addressStreamObserver).onNext(addressGrpcCaptor.capture());

    fr.jufab.grpc.proto.Address addressResponse = addressGrpcCaptor.getValue();
    assertThat(addressResponse).isNotNull()
        .hasFieldOrPropertyWithValue("city", CITY)
        .hasFieldOrPropertyWithValue("street", STREET)
        .hasFieldOrPropertyWithValue("zipCode", ZIP_CODE);

  }

  @Test
  void shouldReturnAnAddress() throws Exception {
    when(addressRepository.getAddressById(ID)).thenReturn(Single.just(address));
    QueryAddress queryAddress = QueryAddress.newBuilder().setId(ID).build();

    addressGrpcService.addressById(queryAddress, addressStreamObserver);

    verify(addressStreamObserver).onCompleted();

    ArgumentCaptor<fr.jufab.grpc.proto.Address> captor =
        ArgumentCaptor.forClass(fr.jufab.grpc.proto.Address.class);
    verify(addressStreamObserver).onNext(captor.capture());

    fr.jufab.grpc.proto.Address addressResponse = captor.getValue();
    assertThat(addressResponse).isNotNull()
        .hasFieldOrPropertyWithValue("city", CITY)
        .hasFieldOrPropertyWithValue("street", STREET)
        .hasFieldOrPropertyWithValue("zipCode", ZIP_CODE);
  }
}