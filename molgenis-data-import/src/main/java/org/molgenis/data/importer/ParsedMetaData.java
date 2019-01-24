package org.molgenis.data.importer;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.molgenis.data.i18n.model.L10nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;

/** Value object to store the result of parsing a source. */
public class ParsedMetaData {
  private final ImmutableMap<String, EntityType> entities;
  private final ImmutableMap<String, Package> packages;
  private final ImmutableMap<String, Tag> tags;
  private final ImmutableMap<String, Language> languages;
  private final ImmutableMap<String, L10nString> l10nStrings;

  public ParsedMetaData(
      List<? extends EntityType> entities,
      Map<String, ? extends Package> packages,
      ImmutableMap<String, Tag> tags,
      Map<String, Language> languages,
      ImmutableMap<String, L10nString> l10nStrings) {
    if (entities == null) {
      throw new NullPointerException("Null entities");
    }

    ImmutableMap.Builder<String, EntityType> builder = ImmutableMap.builder();
    for (EntityType emd : entities) {
      builder.put(emd.getId(), emd);
    }
    this.entities = builder.build();
    if (packages == null) {
      throw new NullPointerException("Null packages");
    }
    this.packages = ImmutableMap.copyOf(packages);
    this.tags = ImmutableMap.copyOf(tags);
    this.languages = ImmutableMap.copyOf(languages);
    this.l10nStrings = ImmutableMap.copyOf(l10nStrings);
  }

  public ImmutableCollection<EntityType> getEntities() {
    return entities.values();
  }

  public ImmutableMap<String, EntityType> getEntityMap() {
    return entities;
  }

  public ImmutableMap<String, Package> getPackages() {
    return packages;
  }

  public ImmutableMap<String, Tag> getTags() {
    return tags;
  }

  public ImmutableMap<String, Language> getLanguages() {
    return languages;
  }

  public ImmutableMap<String, L10nString> getL10nStrings() {
    return l10nStrings;
  }

  @Override
  public String toString() {
    return "ParsedMetaData [entities="
        + entities
        + ", packages="
        + packages
        + ", tags="
        + tags
        + ", languages="
        + languages
        + ", l10nStrings="
        + l10nStrings
        + ']';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParsedMetaData)) {
      return false;
    }
    ParsedMetaData that = (ParsedMetaData) o;
    return Objects.equals(getEntities(), that.getEntities())
        && Objects.equals(getPackages(), that.getPackages())
        && Objects.equals(getTags(), that.getTags())
        && Objects.equals(getLanguages(), that.getLanguages())
        && Objects.equals(getL10nStrings(), that.getL10nStrings());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEntities(), getPackages(), getTags(), getLanguages(), getL10nStrings());
  }
}
