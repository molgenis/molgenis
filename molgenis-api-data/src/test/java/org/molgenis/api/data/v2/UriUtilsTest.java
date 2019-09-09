package org.molgenis.api.data.v2;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class UriUtilsTest {
  @BeforeEach
  void setUpBeforeMethod() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath("/myservlet");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  void testCreateEntityCollectionUriPath() {
    String entityTypeId = "MyEntityTypeId";
    assertEquals(
        UriUtils.createEntityCollectionUriPath(entityTypeId), "/myservlet/api/v2/MyEntityTypeId");
  }

  @Test
  void testCreateEntityTypeMetadataUriPath() {
    String entityTypeId = "MyEntityTypeId";
    // expected: no meta path segment
    assertEquals(
        UriUtils.createEntityTypeMetadataUriPath(entityTypeId), "/myservlet/api/v2/MyEntityTypeId");
  }

  @Test
  void testCreateEntityTypeMetadataAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    assertEquals(
        UriUtils.createEntityTypeMetadataAttributeUriPath(entityTypeId, attributeName),
        "/myservlet/api/v2/MyEntityTypeId/meta/MyAttribute");
  }

  @Test
  void testCreateEntityUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyEntityId";
    assertEquals(
        UriUtils.createEntityUriPath(entityTypeId, entityId),
        "/myservlet/api/v2/MyEntityTypeId/MyEntityId");
  }

  @Test
  void testCreateEntityAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    String entityId = "MyEntityId";
    assertEquals(
        UriUtils.createEntityAttributeUriPath(entityTypeId, entityId, attributeName),
        "/myservlet/api/v2/MyEntityTypeId/MyEntityId/MyAttribute");
  }

  @Test
  void testCreateEntityAttributeUriPathEncoding() {
    String entityTypeId = "/\\?=;*";
    String attributeName = "/\\?=;*";
    String entityId = "/\\?=;*";
    assertEquals(
        UriUtils.createEntityAttributeUriPath(entityTypeId, entityId, attributeName),
        "/myservlet/api/v2/%2F%5C%3F=;*/%2F%5C%3F=;*/%2F%5C%3F=;*");
  }

  @Test
  void testCreateEntitiesUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String idAttributeName = "MyIdAttribute";
    Collection<String> entityIds = asList("MyEntityId0", "MyEntityId1");
    assertEquals(
        UriUtils.createEntitiesUriPath(entityTypeId, idAttributeName, entityIds),
        "/myservlet/api/v2/MyEntityTypeId?q=MyIdAttribute=in=(\"MyEntityId0\",\"MyEntityId1\")");
  }
}
