package org.molgenis.data.importer.emx;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableMap.copyOf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;

/**
 * Mutable bean to store intermediate parse results. Uses lookup tables to map simple names to the
 * parsed objects. Is used by the {@link EmxMetadataParser}
 */
public final class IntermediateParseResults {
  /** Maps full name to EntityType */
  private final Map<String, EntityType> entityTypes;
  /** Maps full name to PackageImpl (with tags) */
  private final Map<String, Package> packages;
  /** Contains all tag entities from the tag sheet */
  private final Map<String, Tag> tags;
  /** Contains all language enities from the languages sheet */
  private final Map<String, Language> languages;
  /** Contains all i18nString entities from the i18nstrings sheet */
  private final Map<String, L10nString> l10nStrings;

  private final EntityTypeFactory entityTypeFactory;

  public IntermediateParseResults(EntityTypeFactory entityTypeFactory) {
    this.entityTypeFactory = entityTypeFactory;
    this.tags = new LinkedHashMap<>();
    this.entityTypes = new LinkedHashMap<>();
    this.packages = new LinkedHashMap<>();
    this.languages = new LinkedHashMap<>();
    this.l10nStrings = new LinkedHashMap<>();
  }

  public void addTag(String identifier, Tag tag) {
    tags.put(identifier, tag);
  }

  public boolean hasTag(String identifier) {
    return tags.containsKey(identifier);
  }

  public Tag getTag(String tagIdentifier) {
    return tags.get(tagIdentifier);
  }

  /**
   * Gets a specific package
   *
   * @param name the name of the package
   */
  public Package getPackage(String name) {
    return getPackages().get(name);
  }

  public void addAttributes(String entityTypeId, List<EmxAttribute> emxAttrs) {
    EntityType entityType = getEntityType(entityTypeId);
    if (entityType == null) entityType = addEntityType(entityTypeId);

    int lookupAttributeIndex = 0;
    for (EmxAttribute emxAttr : emxAttrs) {
      Attribute attr = emxAttr.getAttr();
      entityType.addAttribute(attr);

      // set attribute roles
      if (emxAttr.isIdAttr()) {
        attr.setIdAttribute(true);
      }
      if (emxAttr.isLabelAttr()) {
        attr.setLabelAttribute(true);
        attr.setNillable(false);
      }
      if (emxAttr.isLookupAttr()) {
        attr.setLookupAttributeIndex(lookupAttributeIndex++);
      }
    }

    if (entityType.getLabelAttribute() == null) {
      Attribute ownIdAttr = entityType.getOwnIdAttribute();
      if (ownIdAttr != null && ownIdAttr.isVisible()) {
        ownIdAttr.setLabelAttribute(true);
      }
    }

    if (!entityType.getLookupAttributes().iterator().hasNext()) {
      Attribute ownIdAttr = entityType.getOwnIdAttribute();
      if (ownIdAttr != null && ownIdAttr.isVisible()) {
        ownIdAttr.setLookupAttributeIndex(lookupAttributeIndex++);
      }
      Attribute ownLabelAttr = entityType.getOwnLabelAttribute();
      if (ownLabelAttr != null) {
        ownLabelAttr.setLookupAttributeIndex(lookupAttributeIndex);
      }
    }
  }

  public EntityType addEntityType(String fullyQualifiedName) {
    String entityTypeLabel = fullyQualifiedName;
    Package pack = null;
    for (Package p : packages.values()) {
      String packageName = p.getId();
      if (fullyQualifiedName.toLowerCase().startsWith(packageName.toLowerCase())) {
        entityTypeLabel = fullyQualifiedName.substring(packageName.length() + 1); // package_entity
        pack = p;
      }
    }
    EntityType entityType =
        entityTypeFactory.create(fullyQualifiedName).setLabel(entityTypeLabel).setPackage(pack);
    entityTypes.put(fullyQualifiedName, entityType);
    return entityType;
  }

  public EntityType getEntityType(String name) {
    return entityTypes.get(name);
  }

  public void addLanguage(Language language) {
    languages.put(language.getCode(), language);
  }

  public void addL10nString(L10nString l10nString) {
    l10nStrings.put(l10nString.getMessageID(), l10nString);
  }

  /**
   * Checks if it knows entity with given simple name.
   *
   * @param name simple name of the entity
   * @return true if entity with simple name name is known, false otherwise
   */
  public boolean hasEntityType(String name) {
    return entityTypes.containsKey(name);
  }

  public boolean hasPackage(String name) {
    return packages.containsKey(name);
  }

  public void addPackage(String name, Package p) {
    packages.put(name, p);
  }

  public ImmutableMap<String, EntityType> getEntityMap() {
    return copyOf(entityTypes);
  }

  public ImmutableList<EntityType> getEntityTypes() {
    return copyOf(entityTypes.values());
  }

  public ImmutableMap<String, Package> getPackages() {
    return copyOf(packages);
  }

  public ImmutableMap<String, Tag> getTags() {
    return copyOf(tags);
  }

  public ImmutableMap<String, Language> getLanguages() {
    return copyOf(languages);
  }

  public ImmutableMap<String, L10nString> getL10nStrings() {
    return copyOf(l10nStrings);
  }

  @Override
  public String toString() {
    return "IntermediateParseResults [entities="
        + entityTypes
        + ", packages="
        + packages
        + ", tags="
        + tags
        + ", languages="
        + languages
        + ", l10nStrings="
        + l10nStrings
        + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntermediateParseResults)) {
      return false;
    }
    IntermediateParseResults that = (IntermediateParseResults) o;
    return Objects.equals(getEntityTypes(), that.getEntityTypes())
        && Objects.equals(getPackages(), that.getPackages())
        && Objects.equals(getTags(), that.getTags())
        && Objects.equals(getLanguages(), that.getLanguages())
        && Objects.equals(getL10nStrings(), that.getL10nStrings());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getEntityTypes(), getPackages(), getTags(), getLanguages(), getL10nStrings());
  }
}
