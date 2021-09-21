package org.molgenis.api.data.v2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import org.molgenis.api.ApiNamespace;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

class UriUtils {
  private UriUtils() {}

  /** @return <servletMappingPath>/v2/<entityTypeId> */
  static String createEntityCollectionUriPath(UriComponentsBuilder builder, String entityTypeId) {
    return createEntityCollectionUriComponents(builder, entityTypeId).getPath();
  }

  private static UriComponents createEntityCollectionUriComponents(
      UriComponentsBuilder builder, String entityTypeId) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId);
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId> */
  static String createEntityTypeMetadataUriPath(UriComponentsBuilder builder, String entityTypeId) {
    return createEntityTypeMetadataUriComponents(builder, entityTypeId).getPath();
  }

  private static UriComponents createEntityTypeMetadataUriComponents(
      UriComponentsBuilder builder, String entityTypeId) {
    // v2 doesn't have a dedicated metadata endpoint, the metadata is returned with collection
    return createEntityCollectionUriComponents(builder, entityTypeId);
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/meta/<attributeName> */
  static String createEntityTypeMetadataAttributeUriPath(
      UriComponentsBuilder builder, String entityTypeId, String attributeName) {
    return createEntityTypeMetadataAttributeUriComponents(builder, entityTypeId, attributeName)
        .getPath();
  }

  private static UriComponents createEntityTypeMetadataAttributeUriComponents(
      UriComponentsBuilder builder, String entityTypeId, String attributeName) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId, "meta", attributeName);
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/<entityId> */
  static String createEntityUriPath(
      UriComponentsBuilder builder, String entityTypeId, Object entityId) {
    return createEntityUriComponents(builder, entityTypeId, entityId).getPath();
  }

  private static UriComponents createEntityUriComponents(
      UriComponentsBuilder builder, String entityTypeId, Object entityId) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId, entityId.toString());
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/<entityId>/<attributeName> */
  static String createEntityAttributeUriPath(
      UriComponentsBuilder builder, String entityTypeId, Object entityId, String attributeName) {
    return createEntityAttributeUriComponents(builder, entityTypeId, entityId, attributeName)
        .getPath();
  }

  private static UriComponents createEntityAttributeUriComponents(
      UriComponentsBuilder builder, String entityTypeId, Object entityId, String attributeName) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(
        RestControllerV2.API_VERSION, entityTypeId, entityId.toString(), attributeName);
    return builder.build();
  }

  /**
   * @return
   *     <servletMappingPath>/v1/<entityTypeId>?q=<idAttributeName>=in=("<entityId0>","<entityId1>",...)
   */
  static String createEntitiesUriPath(
      UriComponentsBuilder builder,
      String entityTypeId,
      String idAttributeName,
      Collection<String> entityIds) {
    String path =
        createEntitiesUriComponents(builder, entityTypeId, idAttributeName, entityIds).getPath();
    String query = createEntitiesUriQuery(idAttributeName, entityIds);
    return path + '?' + query;
  }

  private static UriComponents createEntitiesUriComponents(
      UriComponentsBuilder builder,
      String entityTypeId,
      String idAttributeName,
      Collection<String> entityIds) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
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
