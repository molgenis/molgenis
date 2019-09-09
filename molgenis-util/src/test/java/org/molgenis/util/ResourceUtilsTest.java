package org.molgenis.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ResourceUtilsTest {

  @Test
  void getStringClassString() throws IOException {
    assertEquals(ResourceUtils.getString(getClass(), "/resource.txt"), "example resource");
  }

  @Test
  void getStringClassStringCharset() throws IOException {
    assertEquals(ResourceUtils.getString(getClass(), "/resource.txt", UTF_8), "example resource");
  }

  @Test
  void getBytes() throws IOException {
    assertEquals(ResourceUtils.getBytes(getClass(), "/resource.txt").length, 16);
  }

  @Test
  void getMySqlQueryFromFileTest() throws IOException {
    String query = ResourceUtils.getString(getClass(), "/test_mysql_repo_util_query.sql");
    assertEquals(query, "SELECT * FROM `test` WHERE `test`='test';");
  }
}
