package fr.jufab.graphql.datafetcher;

import fr.jufab.database.dto.Address;
import fr.jufab.database.repositories.AddressRepository;
import graphql.schema.DataFetchingEnvironment;
import io.helidon.common.reactive.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressDataFetcherTest {

  public static final String STREET = "street";
  public static final String ZIP_CODE = "zipCode";
  public static final String CITY = "city";
  public static final int ID = 1;
  @Mock
  AddressRepository addressRepository;
  @Spy
  DataFetchingEnvironment environment;
  @InjectMocks
  AddressDataFetcher addressDataFetcher;

  Address address;

  @BeforeEach
  void initTest() {
    address = Address.builder().id(ID).street(STREET).zipCode(ZIP_CODE).city(CITY).build();
  }

  @Test
  void shouldReturnAnAddress() throws Exception {
    when(environment.getArgument("id")).thenReturn(ID);
    when(addressRepository.getAddressById(anyInt())).thenReturn(Single.just(address));
    Address addressResult = addressDataFetcher.getAddressById().get(environment);
    assertThat(addressResult).isNotNull()
        .hasFieldOrPropertyWithValue("city", CITY)
        .hasFieldOrPropertyWithValue("street", STREET)
        .hasFieldOrPropertyWithValue("zipCode", ZIP_CODE);
  }
}