package org.molgenis.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.util.ResourceUtils.getString;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ResourceUtilsTest {

  @Test
  void getStringClassString() throws IOException {
    assertEquals("example resource", getString(getClass(), "/resource.txt"));
  }

  @Test
  void getStringClassStringCharset() throws IOException {
    assertEquals("example resource", getString(getClass(), "/resource.txt", UTF_8));
  }

  @Test
  void getBytes() throws IOException {
    assertEquals(16, ResourceUtils.getBytes(getClass(), "/resource.txt").length);
  }

  @Test
  void getMySqlQueryFromFileTest() throws IOException {
    String query = ResourceUtils.getString(getClass(), "/test_mysql_repo_util_query.sql");
    assertEquals("SELECT * FROM `test` WHERE `test`='test';", query);
  }
}
