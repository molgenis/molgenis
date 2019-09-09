package org.molgenis.api.data.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        UriUtils.createEntityCollectionUriPath(entityTypeId), "/myservlet/api/v1/MyEntityTypeId");
  }

  @Test
  void testCreateEntityTypeMetadataUriPath() {
    String entityTypeId = "MyEntityTypeId";
    assertEquals(
        UriUtils.createEntityTypeMetadataUriPath(entityTypeId),
        "/myservlet/api/v1/MyEntityTypeId/meta");
  }

  @Test
  void testCreateEntityTypeMetadataAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    assertEquals(
        UriUtils.createEntityTypeMetadataAttributeUriPath(entityTypeId, attributeName),
        "/myservlet/api/v1/MyEntityTypeId/meta/MyAttribute");
  }

  @Test
  void testCreateEntityUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyEntityId";
    assertEquals(
        UriUtils.createEntityUriPath(entityTypeId, entityId),
        "/myservlet/api/v1/MyEntityTypeId/MyEntityId");
  }

  @Test
  void testCreateEntityAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    String entityId = "MyEntityId";
    assertEquals(
        UriUtils.createEntityAttributeUriPath(entityTypeId, entityId, attributeName),
        "/myservlet/api/v1/MyEntityTypeId/MyEntityId/MyAttribute");
  }

  @Test
  void testCreateEntityAttributeUriPathEncoding() {
    String entityTypeId = "/\\?=;*";
    String attributeName = "/\\?=;*";
    String entityId = "/\\?=;*";
    assertEquals(
        UriUtils.createEntityAttributeUriPath(entityTypeId, entityId, attributeName),
        "/myservlet/api/v1/%2F%5C%3F=;*/%2F%5C%3F=;*/%2F%5C%3F=;*");
  }
}
