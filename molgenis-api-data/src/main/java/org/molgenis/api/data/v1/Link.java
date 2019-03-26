package org.molgenis.api.data.v1;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class Link {
  abstract String getHref();

  abstract String getHrefCollection();

  public static Link create(String href, String hrefCollection) {
    return new AutoValue_Link(href, hrefCollection);
  }
}
