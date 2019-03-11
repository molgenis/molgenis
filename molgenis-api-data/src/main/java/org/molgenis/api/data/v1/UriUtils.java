package org.molgenis.api.data.v1;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

class UriUtils {
  private UriUtils() {}

  /** @return <servletMappingPath>/v1/<entityTypeId> */
  static String createEntityCollectionUriPath(String entityTypeId) {
    return createEntityCollectionUriComponents(entityTypeId).getPath();
  }

  private static UriComponents createEntityCollectionUriComponents(String entityTypeId) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestController.API_VERSION, entityTypeId);
    return builder.build();
  }

  /** @return <servletMappingPath>/v1/<entityTypeId>/meta */
  static String createEntityTypeMetadataUriPath(String entityTypeId) {
    return createEntityTypeMetadataUriComponents(entityTypeId).getPath();
  }

  private static UriComponents createEntityTypeMetadataUriComponents(String entityTypeId) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestController.API_VERSION, entityTypeId, "meta");
    return builder.build();
  }

  /** @return <servletMappingPath>/v1/<entityTypeId>/meta/<attributeName> */
  static String createEntityTypeMetadataAttributeUriPath(
      String entityTypeId, String attributeName) {
    return createEntityTypeMetadataAttributeUriComponents(entityTypeId, attributeName).getPath();
  }

  private static UriComponents createEntityTypeMetadataAttributeUriComponents(
      String entityTypeId, String attributeName) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestController.API_VERSION, entityTypeId, "meta", attributeName);
    return builder.build();
  }

  /** @return <servletMappingPath>/v1/<entityTypeId>/<entityId> */
  static String createEntityUriPath(String entityTypeId, Object entityId) {
    return createEntityUriComponents(entityTypeId, entityId).getPath();
  }

  private static UriComponents createEntityUriComponents(String entityTypeId, Object entityId) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestController.API_VERSION, entityTypeId, entityId.toString());
    return builder.build();
  }

  /** @return <servletMappingPath>/v1/<entityTypeId>/<entityId>/<attributeName> */
  static String createEntityAttributeUriPath(
      String entityTypeId, Object entityId, String attributeName) {
    return createEntityAttributeUriComponents(entityTypeId, entityId, attributeName).getPath();
  }

  private static UriComponents createEntityAttributeUriComponents(
      String entityTypeId, Object entityId, String attributeName) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(
        RestController.API_VERSION, entityTypeId, entityId.toString(), attributeName);
    return builder.build();
  }

  private static ServletUriComponentsBuilder createBuilder() {
    ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentServletMapping();
    builder.encode();
    return builder;
  }
}
