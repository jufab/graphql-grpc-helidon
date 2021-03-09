package fr.jufab.database.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author jufab
 * @version 1.0
 */
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address implements Serializable {
  int id;
  String street;
  String zipCode;
  String city;

  public Address(int id) {
    this.id = id;
  }

  public Address(String street, String zipCode, String city) {
    this.street = street;
    this.zipCode = zipCode;
    this.city = city;
  }
}
