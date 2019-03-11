package org.molgenis.api.data.v2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

class UriUtils {
  private UriUtils() {}

  /** @return <servletMappingPath>/v2/<entityTypeId> */
  static String createEntityCollectionUriPath(String entityTypeId) {
    return createEntityCollectionUriComponents(entityTypeId).getPath();
  }

  private static UriComponents createEntityCollectionUriComponents(String entityTypeId) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId);
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId> */
  static String createEntityTypeMetadataUriPath(String entityTypeId) {
    return createEntityTypeMetadataUriComponents(entityTypeId).getPath();
  }

  private static UriComponents createEntityTypeMetadataUriComponents(String entityTypeId) {
    // v2 doesn't have a dedicated metadata endpoint, the metadata is returned with collection
    return createEntityCollectionUriComponents(entityTypeId);
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/meta/<attributeName> */
  static String createEntityTypeMetadataAttributeUriPath(
      String entityTypeId, String attributeName) {
    return createEntityTypeMetadataAttributeUriComponents(entityTypeId, attributeName).getPath();
  }

  private static UriComponents createEntityTypeMetadataAttributeUriComponents(
      String entityTypeId, String attributeName) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId, "meta", attributeName);
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/<entityId> */
  static String createEntityUriPath(String entityTypeId, Object entityId) {
    return createEntityUriComponents(entityTypeId, entityId).getPath();
  }

  private static UriComponents createEntityUriComponents(String entityTypeId, Object entityId) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId, entityId.toString());
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/<entityId>/<attributeName> */
  static String createEntityAttributeUriPath(
      String entityTypeId, Object entityId, String attributeName) {
    return createEntityAttributeUriComponents(entityTypeId, entityId, attributeName).getPath();
  }

  private static UriComponents createEntityAttributeUriComponents(
      String entityTypeId, Object entityId, String attributeName) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(
        RestControllerV2.API_VERSION, entityTypeId, entityId.toString(), attributeName);
    return builder.build();
  }

  private static ServletUriComponentsBuilder createBuilder() {
    ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentServletMapping();
    builder.encode();
    return builder;
  }

  /**
   * @return
   *     <servletMappingPath>/v1/<entityTypeId>?q=<idAttributeName>=in=("<entityId0>","<entityId1>",...)
   */
  static String createEntitiesUriPath(
      String entityTypeId, String idAttributeName, Collection<String> entityIds) {
    String path = createEntitiesUriComponents(entityTypeId, idAttributeName, entityIds).getPath();
    String query = createEntitiesUriQuery(idAttributeName, entityIds);
    return path + '?' + query;
  }

  private static UriComponents createEntitiesUriComponents(
      String entityTypeId, String idAttributeName, Collection<String> entityIds) {
    ServletUriComponentsBuilder builder = createBuilder();
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId);
    builder.queryParam("q", createEntitiesUriQuery(idAttributeName, entityIds));
    return builder.build();
  }

  private static String createEntitiesUriQuery(
      String idAttributeName, Collection<String> entityIds) {
    String inQueryValue = entityIds.stream().map(UriUtils::encodeEntityId).collect(joining(","));
    return String.format("q=%s=in=(%s)", idAttributeName, inQueryValue);
  }

  private static String encodeEntityId(String entityId) {
    return '"' + org.springframework.web.util.UriUtils.encodePathSegment(entityId, UTF_8) + '"';
  }
}
