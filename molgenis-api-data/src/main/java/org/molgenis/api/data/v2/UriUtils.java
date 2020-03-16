package org.molgenis.api.data.v2;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import org.molgenis.api.ApiNamespace;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

class UriUtils {
  private UriUtils() {}

  /** @return <servletMappingPath>/v2/<entityTypeId> */
  static String createEntityCollectionUriPath(
      ServletUriComponentsBuilder builder, String entityTypeId) {
    return createEntityCollectionUriComponents(builder, entityTypeId).getPath();
  }

  private static UriComponents createEntityCollectionUriComponents(
      ServletUriComponentsBuilder builder, String entityTypeId) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId);
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId> */
  static String createEntityTypeMetadataUriPath(
      ServletUriComponentsBuilder builder, String entityTypeId) {
    return createEntityTypeMetadataUriComponents(builder, entityTypeId).getPath();
  }

  private static UriComponents createEntityTypeMetadataUriComponents(
      ServletUriComponentsBuilder builder, String entityTypeId) {
    // v2 doesn't have a dedicated metadata endpoint, the metadata is returned with collection
    return createEntityCollectionUriComponents(builder, entityTypeId);
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/meta/<attributeName> */
  static String createEntityTypeMetadataAttributeUriPath(
      ServletUriComponentsBuilder builder, String entityTypeId, String attributeName) {
    return createEntityTypeMetadataAttributeUriComponents(builder, entityTypeId, attributeName)
        .getPath();
  }

  private static UriComponents createEntityTypeMetadataAttributeUriComponents(
      ServletUriComponentsBuilder builder, String entityTypeId, String attributeName) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId, "meta", attributeName);
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/<entityId> */
  static String createEntityUriPath(
      ServletUriComponentsBuilder builder, String entityTypeId, Object entityId) {
    return createEntityUriComponents(builder, entityTypeId, entityId).getPath();
  }

  private static UriComponents createEntityUriComponents(
      ServletUriComponentsBuilder builder, String entityTypeId, Object entityId) {
    builder = builder.cloneBuilder();
    builder.path(ApiNamespace.API_PATH);
    builder.pathSegment(RestControllerV2.API_VERSION, entityTypeId, entityId.toString());
    return builder.build();
  }

  /** @return <servletMappingPath>/v2/<entityTypeId>/<entityId>/<attributeName> */
  static String createEntityAttributeUriPath(
      ServletUriComponentsBuilder builder,
      String entityTypeId,
      Object entityId,
      String attributeName) {
    return createEntityAttributeUriComponents(builder, entityTypeId, entityId, attributeName)
        .getPath();
  }

  private static UriComponents createEntityAttributeUriComponents(
      ServletUriComponentsBuilder builder,
      String entityTypeId,
      Object entityId,
      String attributeName) {
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
      ServletUriComponentsBuilder builder,
      String entityTypeId,
      String idAttributeName,
      Collection<String> entityIds) {
    String path =
        createEntitiesUriComponents(builder, entityTypeId, idAttributeName, entityIds).getPath();
    String query = createEntitiesUriQuery(idAttributeName, entityIds);
    return path + '?' + query;
  }

  private static UriComponents createEntitiesUriComponents(
      ServletUriComponentsBuilder builder,
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
