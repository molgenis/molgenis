package org.molgenis.validation;

import static org.testng.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.molgenis.validation.exception.LocalhostNotAllowedException;
import org.molgenis.validation.exception.RelativePathNotAllowedException;
import org.testng.annotations.Test;

public class UriValidatorTest {

  @Test(expectedExceptions = RelativePathNotAllowedException.class)
  public void testIllegalPath() throws URISyntaxException, UnknownHostException {
    UriValidator.getSafeUri("/path/to/file1.txt");
  }

  @Test(expectedExceptions = RelativePathNotAllowedException.class)
  public void testIllegalPath2() throws URISyntaxException, UnknownHostException {
    UriValidator.getSafeUri("../path/to/file1.txt");
  }

  @Test(expectedExceptions = RelativePathNotAllowedException.class)
  public void testIllegalPath3() throws URISyntaxException, UnknownHostException {
    UriValidator.getSafeUri("/path/to/../../../file1.txt");
  }

  @Test(expectedExceptions = LocalhostNotAllowedException.class)
  public void testLocalhost() throws URISyntaxException, UnknownHostException {
    UriValidator.getSafeUri("http://localhost:8080/file1.txt");
  }

  @Test(expectedExceptions = LocalhostNotAllowedException.class)
  public void testLocalhost2() throws URISyntaxException, UnknownHostException {
    UriValidator.getSafeUri("https://localhost/file1.txt");
  }

  @Test
  public void testValid1() throws URISyntaxException, UnknownHostException, URISyntaxException {
    URI actual = UriValidator.getSafeUri("ftp://www.molgenis.org/file1.txt");
    URI expected = new URI("ftp://www.molgenis.org/file1.txt");
    assertEquals(actual, expected);
  }

  @Test
  public void testValid2() throws URISyntaxException, UnknownHostException {
    URI actual = UriValidator.getSafeUri("https://www.molgenis.org/file1.txt");
    URI expected = new URI("https://www.molgenis.org/file1.txt");
    assertEquals(actual, expected);
  }

  @Test
  public void testValid3() throws URISyntaxException, UnknownHostException {
    URI actual = UriValidator.getSafeUri("http://www.molgenis.org/file1.txt");
    URI expected = new URI("http://www.molgenis.org/file1.txt");
    assertEquals(actual, expected);
  }
}
