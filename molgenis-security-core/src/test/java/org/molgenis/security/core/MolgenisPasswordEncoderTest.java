package org.molgenis.security.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class MolgenisPasswordEncoderTest {

  @Test
  void MolgenisPasswordEncoder() {
    assertThrows(IllegalArgumentException.class, () -> new MolgenisPasswordEncoder(null));
  }

  @Test
  void encode() {
    String password = "password";
    String encodedPassword = "encoded-password";
    BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    when(bCryptPasswordEncoder.encode(password)).thenReturn(encodedPassword);
    assertEquals(
        encodedPassword, new MolgenisPasswordEncoder(bCryptPasswordEncoder).encode(password));
  }

  @Test
  void matches() {
    String password = "password";
    String encodedPassword = "encoded-password";
    BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    when(bCryptPasswordEncoder.matches(password, encodedPassword)).thenReturn(true);
    assertTrue(
        new MolgenisPasswordEncoder(bCryptPasswordEncoder).matches(password, encodedPassword));
  }

  @Test
  void matches_noMatch() {
    String password = "password";
    String encodedPassword = "encoded-password";
    BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);
    when(bCryptPasswordEncoder.matches(password, encodedPassword)).thenReturn(true);
    assertFalse(
        new MolgenisPasswordEncoder(bCryptPasswordEncoder)
            .matches("invalid-password", encodedPassword));
  }
}
