package org.molgenis.ontology.sorta.controller;

import static org.molgenis.i18n.LanguageService.getCurrentUserLanguageCode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;

public class EntityTypeResponse {
  private static final String BASE_URI = "/api/v1";

  private final String href;
  private final String hrefCollection;
  private final String name;
  private final String label;
  private final String description;
  private final Map<String, Object> attributes;
  private final String labelAttribute;
  private final String idAttribute;
  private final List<String> lookupAttributes;
  private final Boolean isAbstract;
  private String languageCode;
  private final Set<Permission> permissions;

  /**
   * Is this user allowed to add/update/delete data of this type and has the repo the capability?
   */
  private final Boolean writable;

  public EntityTypeResponse(
      EntityType meta, UserPermissionEvaluator userPermissionEvaluator, DataService dataService) {
    this(meta, null, null, userPermissionEvaluator, dataService);
  }

  /**
   * @param attributesSet set of lowercase attribute names to include in response
   * @param attributeExpandsSet set of lowercase attribute names to expand in response
   */
  public EntityTypeResponse(
      EntityType meta,
      Set<String> attributesSet,
      Map<String, Set<String>> attributeExpandsSet,
      UserPermissionEvaluator userPermissionEvaluator,
      DataService dataService) {
    String metaId = meta.getId();
    this.href = Href.concatMetaEntityHref(BASE_URI, metaId);
    this.hrefCollection = String.format("%s/%s", BASE_URI, metaId); // FIXME apply Href escaping fix
    this.languageCode = getCurrentUserLanguageCode();

    if (attributesSet == null || attributesSet.contains("name".toLowerCase())) {
      this.name = metaId;
    } else this.name = null;

    if (attributesSet == null || attributesSet.contains("description".toLowerCase())) {
      this.description = meta.getDescription(getCurrentUserLanguageCode());
    } else this.description = null;

    if (attributesSet == null || attributesSet.contains("label".toLowerCase())) {
      label = meta.getLabel(getCurrentUserLanguageCode());
    } else this.label = null;

    if (attributesSet == null || attributesSet.contains("attributes".toLowerCase())) {
      this.attributes = new LinkedHashMap<>();
      // the newArraylist is a fix for concurrency trouble
      // FIXME properly fix this by making metadata immutable
      for (Attribute attr : Lists.newArrayList(meta.getAttributes())) {
        if (!attr.getName().equals("__Type")) {
          if (attributeExpandsSet != null
              && attributeExpandsSet.containsKey("attributes".toLowerCase())) {
            Set<String> subAttributesSet = attributeExpandsSet.get("attributes".toLowerCase());
            this.attributes.put(
                attr.getName(),
                new AttributeResponse(
                    metaId,
                    meta,
                    attr,
                    subAttributesSet,
                    Collections.singletonMap(
                        "refEntity".toLowerCase(), Sets.newHashSet("idattribute")),
                    userPermissionEvaluator,
                    dataService));
          } else {
            String attrHref = Href.concatMetaAttributeHref(BASE_URI, metaId, attr.getName());
            this.attributes.put(attr.getName(), Collections.singletonMap("href", attrHref));
          }
        }
      }
    } else this.attributes = null;

    if (attributesSet == null || attributesSet.contains("labelAttribute".toLowerCase())) {
      Attribute metaLabelAttribute = meta.getLabelAttribute(this.languageCode);
      this.labelAttribute = metaLabelAttribute != null ? metaLabelAttribute.getName() : null;
    } else this.labelAttribute = null;

    if (attributesSet == null || attributesSet.contains("idAttribute".toLowerCase())) {
      Attribute metaIdAttribute = meta.getIdAttribute();
      this.idAttribute = metaIdAttribute != null ? metaIdAttribute.getName() : null;
    } else this.idAttribute = null;

    if (attributesSet == null || attributesSet.contains("lookupAttributes".toLowerCase())) {
      Iterable<Attribute> metaLookupAttributes = meta.getLookupAttributes();
      this.lookupAttributes =
          metaLookupAttributes != null
              ? Lists.newArrayList(Iterables.transform(metaLookupAttributes, Attribute::getName))
              : null;
    } else this.lookupAttributes = null;

    if (attributesSet == null || attributesSet.contains("abstract".toLowerCase())) {
      isAbstract = meta.isAbstract();
    } else this.isAbstract = null;

    boolean hasWritePermission =
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(metaId), EntityTypePermission.UPDATE_DATA);
    boolean hasWriteMetaPermission =
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity(metaId), EntityTypePermission.UPDATE_METADATA);
    this.writable =
        (hasWritePermission || hasWriteMetaPermission)
            && dataService.getCapabilities(metaId).contains(RepositoryCapability.WRITABLE);

    this.permissions =
        userPermissionEvaluator.getPermissions(
            new EntityTypeIdentity(metaId), EntityTypePermission.values());
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

  public Map<String, Object> getAttributes() {
    return ImmutableMap.copyOf(attributes);
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
