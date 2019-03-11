package org.molgenis.api.data.v2;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UriUtilsTest {
  @BeforeMethod
  public void setUpBeforeMethod() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath("/myapi");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  public void testCreateEntityCollectionUriPath() {
    String entityTypeId = "MyEntityTypeId";
    assertEquals(UriUtils.createEntityCollectionUriPath(entityTypeId), "/myapi/v2/MyEntityTypeId");
  }

  @Test
  public void testCreateEntityTypeMetadataUriPath() {
    String entityTypeId = "MyEntityTypeId";
    // expected: no meta path segment
    assertEquals(
        UriUtils.createEntityTypeMetadataUriPath(entityTypeId), "/myapi/v2/MyEntityTypeId");
  }

  @Test
  public void testCreateEntityTypeMetadataAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    assertEquals(
        UriUtils.createEntityTypeMetadataAttributeUriPath(entityTypeId, attributeName),
        "/myapi/v2/MyEntityTypeId/meta/MyAttribute");
  }

  @Test
  public void testCreateEntityUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String entityId = "MyEntityId";
    assertEquals(
        UriUtils.createEntityUriPath(entityTypeId, entityId),
        "/myapi/v2/MyEntityTypeId/MyEntityId");
  }

  @Test
  public void testCreateEntityAttributeUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String attributeName = "MyAttribute";
    String entityId = "MyEntityId";
    assertEquals(
        UriUtils.createEntityAttributeUriPath(entityTypeId, entityId, attributeName),
        "/myapi/v2/MyEntityTypeId/MyEntityId/MyAttribute");
  }

  @Test
  public void testCreateEntityAttributeUriPathEncoding() {
    String entityTypeId = "/\\?=;*";
    String attributeName = "/\\?=;*";
    String entityId = "/\\?=;*";
    assertEquals(
        UriUtils.createEntityAttributeUriPath(entityTypeId, entityId, attributeName),
        "/myapi/v2/%2F%5C%3F=;*/%2F%5C%3F=;*/%2F%5C%3F=;*");
  }

  @Test
  public void testCreateEntitiesUriPath() {
    String entityTypeId = "MyEntityTypeId";
    String idAttributeName = "MyIdAttribute";
    Collection<String> entityIds = asList("MyEntityId0", "MyEntityId1");
    assertEquals(
        UriUtils.createEntitiesUriPath(entityTypeId, idAttributeName, entityIds),
        "/myapi/v2/MyEntityTypeId?q=MyIdAttribute=in=(\"MyEntityId0\",\"MyEntityId1\")");
  }
}
