package org.molgenis.navigator;

import com.google.auto.value.AutoValue;
import org.molgenis.web.rsql.QueryRsql;

@AutoValue
public abstract class SearchResourcesRequest {
  public abstract QueryRsql getQuery();

  public static SearchResourcesRequest create(QueryRsql newQuery) {
    return new AutoValue_SearchResourcesRequest(newQuery);
  }
}
