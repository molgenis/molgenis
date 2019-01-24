package org.molgenis.data.rest.v2;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Streams.stream;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.rest.v2.AttributeResponseV2.filterAttributes;
import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;
import static org.molgenis.i18n.LanguageService.getCurrentUserLanguageCode;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.molgenis.data.DataService;
import org.molgenis.data.Fetch;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.util.Href;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.util.EntityTypeUtils;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;

class EntityTypeResponseV2 {
  private final String href;
  private final String hrefCollection;
  private final String name;
  private final String label;
  private final String description;
  private final List<AttributeResponseV2> attributes;
  private final String labelAttribute;
  private final String idAttribute;
  private final List<String> lookupAttributes;
  private final Boolean isAbstract;
  /** Is this user allowed to add/update/delete entities of this type? */
  private final Boolean writable;

  private String languageCode;
  private final Set<Permission> permissions;

  public EntityTypeResponseV2(
      EntityType meta,
      UserPermissionEvaluator userPermissionEvaluator,
      DataService dataService,
      boolean includeCategories) {
    this(meta, null, userPermissionEvaluator, dataService, includeCategories);
  }

  /** @param fetch set of lowercase attribute names to include in response */
  public EntityTypeResponseV2(
      EntityType meta,
      Fetch fetch,
      UserPermissionEvaluator userPermissionEvaluator,
      DataService dataService,
      boolean includeCategories) {
    String name = meta.getId();
    this.href = Href.concatMetaEntityHrefV2(BASE_URI, name);
    this.hrefCollection = String.format("%s/%s", BASE_URI, name); // FIXME apply Href escaping fix

    this.name = name;
    this.description = meta.getDescription(getCurrentUserLanguageCode());
    this.label = meta.getLabel(getCurrentUserLanguageCode());

    // filter attribute parts
    Iterable<Attribute> filteredAttrs = filterAttributes(fetch, meta.getAttributes());

    this.attributes =
        stream(filteredAttrs)
            .map(
                attr -> {
                  Fetch subAttrFetch = null;
                  if (fetch != null) {
                    if (attr.getDataType().equals(COMPOUND)) {
                      subAttrFetch = fetch;
                    } else {
                      subAttrFetch = fetch.getFetch(attr);
                    }
                  } else if (EntityTypeUtils.isReferenceType(attr)) {
                    subAttrFetch =
                        AttributeFilterToFetchConverter.createDefaultAttributeFetch(
                            attr, languageCode);
                  }
                  return new AttributeResponseV2(
                      name,
                      meta,
                      attr,
                      subAttrFetch,
                      userPermissionEvaluator,
                      dataService,
                      includeCategories);
                })
            .collect(Collectors.toList());

    languageCode = getCurrentUserLanguageCode();

    Attribute labelAttribute = meta.getLabelAttribute(languageCode);
    this.labelAttribute = labelAttribute != null ? labelAttribute.getName() : null;

    Attribute idAttribute = meta.getIdAttribute();
    this.idAttribute = idAttribute != null ? idAttribute.getName() : null;

    Iterable<Attribute> lookupAttributes = meta.getLookupAttributes();
    this.lookupAttributes =
        lookupAttributes != null
            ? newArrayList(Iterables.transform(lookupAttributes, Attribute::getName))
            : null;

    this.isAbstract = meta.isAbstract();

    this.writable =
        userPermissionEvaluator.hasPermission(
                new EntityTypeIdentity(name), EntityTypePermission.UPDATE_DATA)
            && dataService.getCapabilities(name).contains(RepositoryCapability.WRITABLE);

    this.permissions =
        userPermissionEvaluator.getPermissions(
            new EntityTypeIdentity(name), EntityTypePermission.values());
  }

  public String getHref() {
    return href;
  }

  public String getHrefCollection() {
    return hrefCollection;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getIdAttribute() {
    return idAttribute;
  }

  public List<String> getLookupAttributes() {
    return lookupAttributes;
  }

  public List<AttributeResponseV2> getAttributes() {
    return attributes;
  }

  public String getLabelAttribute() {
    return labelAttribute;
  }

  public boolean isAbstract() {
    return isAbstract;
  }

  public Boolean getWritable() {
    return writable;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
}
