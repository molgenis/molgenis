package org.molgenis.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.junit.jupiter.api.Test;
import org.molgenis.validation.exception.LocalhostNotAllowedException;
import org.molgenis.validation.exception.RelativePathNotAllowedException;

class UriValidatorTest {

  @Test
  void testIllegalPath() {
    assertThrows(
        RelativePathNotAllowedException.class, () -> UriValidator.getSafeUri("/path/to/file1.txt"));
  }

  @Test
  void testIllegalPath2() {
    assertThrows(
        RelativePathNotAllowedException.class,
        () -> UriValidator.getSafeUri("../path/to/file1.txt"));
  }

  @Test
  void testIllegalPath3() {
    assertThrows(
        RelativePathNotAllowedException.class,
        () -> UriValidator.getSafeUri("/path/to/../../../file1.txt"));
  }

  @Test
  void testLocalhost() {
    assertThrows(
        LocalhostNotAllowedException.class,
        () -> UriValidator.getSafeUri("http://localhost:8080/file1.txt"));
  }

  @Test
  void testLocalhost2() {
    assertThrows(
        LocalhostNotAllowedException.class,
        () -> UriValidator.getSafeUri("https://localhost/file1.txt"));
  }

  @Test
  void testValid1() throws UnknownHostException, URISyntaxException {
    URI actual = UriValidator.getSafeUri("ftp://www.molgenis.org/file1.txt");
    URI expected = new URI("ftp://www.molgenis.org/file1.txt");
    assertEquals(expected, actual);
  }

  @Test
  void testValid2() throws URISyntaxException, UnknownHostException {
    URI actual = UriValidator.getSafeUri("https://www.molgenis.org/file1.txt");
    URI expected = new URI("https://www.molgenis.org/file1.txt");
    assertEquals(expected, actual);
  }

  @Test
  void testValid3() throws URISyntaxException, UnknownHostException {
    URI actual = UriValidator.getSafeUri("http://www.molgenis.org/file1.txt");
    URI expected = new URI("http://www.molgenis.org/file1.txt");
    assertEquals(expected, actual);
  }
}
