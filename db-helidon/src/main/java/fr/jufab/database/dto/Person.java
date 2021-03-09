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
public class Person implements Serializable {
  int id;
  String firstname;
  String lastname;
  int age;
  Address address;
  Gender gender;

  public Person(String firstname, String lastname, int age,
      Address address, Gender gender) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.age = age;
    this.address = address;
    this.gender = gender;
  }
}
