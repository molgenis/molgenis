package org.molgenis.api.data.v2;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.api.data.v2.UriUtils.createEntitiesUriPath;
import static org.molgenis.api.data.v2.UriUtils.createEntityAttributeUriPath;
import static org.molgenis.api.data.v2.UriUtils.createEntityCollectionUriPath;
import static org.molgenis.api.data.v2.UriUtils.createEntityTypeMetadataAttributeUriPath;
import static org.molgenis.api.data.v2.UriUtils.createEntityTypeMetadataUriPath;
import static org.molgenis.api.data.v2.UriUtils.createEntityUriPath;

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
    assertEquals("/myservlet/api/v2/MyEntityTypeId", createEntityCollectionUriPath(entityTypeId));
  }

  @Test
  void testCreateEntityTypeMetadataUriPath() {
    String entityTypeId = "MyEntityTypeId";
    // expected: no meta path segment
    assertEquals("/myservlet/api/v2/MyEntityTypeId", createEntityTypeMetadataUriPath(entityTypeId));
  }

  @Test
  void testCreateEntityTypeMetadataAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    assertEquals(
        "/myservlet/api/v2/MyEntityTypeId/meta/MyAttribute",
        createEntityTypeMetadataAttributeUriPath(entityTypeId, attributeName));
  }

  @Test
  void testCreateEntityUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyEntityId";
    assertEquals(
        "/myservlet/api/v2/MyEntityTypeId/MyEntityId", createEntityUriPath(entityTypeId, entityId));
  }

  @Test
  void testCreateEntityAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    String entityId = "MyEntityId";
    assertEquals(
        "/myservlet/api/v2/MyEntityTypeId/MyEntityId/MyAttribute",
        createEntityAttributeUriPath(entityTypeId, entityId, attributeName));
  }

  @Test
  void testCreateEntityAttributeUriPathEncoding() {
    String entityTypeId = "/\\?=;*";
    String attributeName = "/\\?=;*";
    String entityId = "/\\?=;*";
    assertEquals(
        "/myservlet/api/v2/%2F%5C%3F=;*/%2F%5C%3F=;*/%2F%5C%3F=;*",
        createEntityAttributeUriPath(entityTypeId, entityId, attributeName));
  }

  @Test
  void testCreateEntitiesUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String idAttributeName = "MyIdAttribute";
    Collection<String> entityIds = asList("MyEntityId0", "MyEntityId1");
    assertEquals(
        "/myservlet/api/v2/MyEntityTypeId?q=MyIdAttribute=in=(\"MyEntityId0\",\"MyEntityId1\")",
        createEntitiesUriPath(entityTypeId, idAttributeName, entityIds));
  }
}
