package org.molgenis.api.data.v2;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import org.molgenis.api.data.v1.EntityPager;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.web.util.UriComponentsBuilder;

class EntityCollectionResponseV2 {
  private final String href;
  private final EntityTypeResponseV2 meta;
  private final Integer start;
  private final Integer num;
  private final Long total;
  private final String prevHref;
  private final String nextHref;
  private final List<Map<String, Object>> items;

  public EntityCollectionResponseV2(String href) {
    this.href = requireNonNull(href);
    this.meta = null;
    this.start = null;
    this.num = null;
    this.total = null;
    this.prevHref = null;
    this.nextHref = null;
    this.items = null;
  }

  public EntityCollectionResponseV2(
      UriComponentsBuilder uriBuilder,
      EntityPager entityPager,
      List<Map<String, Object>> items,
      Fetch fetch,
      String href,
      EntityType meta,
      UserPermissionEvaluator permissionService,
      DataService dataService,
      String prevHref,
      String nextHref,
      boolean includeCategories) {
    this.href = href;
    this.meta =
        new EntityTypeResponseV2(
            uriBuilder, meta, fetch, permissionService, dataService, includeCategories);
    this.start = entityPager.getStart();
    this.num = entityPager.getNum();
    this.total = entityPager.getTotal();
    this.prevHref = prevHref;
    this.nextHref = nextHref;
    this.items = items;
  }

  public String getHref() {
    return href;
  }

  public EntityTypeResponseV2 getMeta() {
    return meta;
  }

  public int getStart() {
    return start;
  }

  public int getNum() {
    return num;
  }

  public long getTotal() {
    return total;
  }

  public String getPrevHref() {
    return prevHref;
  }

  public String getNextHref() {
    return nextHref;
  }

  public List<Map<String, Object>> getItems() {
    return items;
  }
}
