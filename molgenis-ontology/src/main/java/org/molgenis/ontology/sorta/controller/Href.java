package org.molgenis.ontology.sorta.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.springframework.web.util.UriUtils;

public class Href {
  private final String href;
  private final String hrefCollection;

  public Href(String href, String hrefCollection) {
    this.href = href;
    this.hrefCollection = hrefCollection;
  }

  public String getHref() {
    return href;
  }

  public String getHrefCollection() {
    return hrefCollection;
  }

  /** Create an encoded href for an attribute meta */
  public static String concatMetaAttributeHref(
      String baseUri, String entityParentName, String attributeName) {
    return String.format(
        "%s/%s/meta/%s",
        baseUri, encodePathSegment(entityParentName), encodePathSegment(attributeName));
  }

  /** Create an encoded href for an entity meta */
  public static String concatMetaEntityHref(String baseUri, String qualifiedEntityName) {
    return String.format("%s/%s/meta", baseUri, encodePathSegment(qualifiedEntityName));
  }

  private static String encodePathSegment(String pathSegment) {
    return UriUtils.encodePathSegment(pathSegment, UTF_8.name());
  }
}
