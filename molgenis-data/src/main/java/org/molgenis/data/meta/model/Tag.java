package org.molgenis.data.meta.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.eclipse.rdf4j.model.IRI;
import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

// TODO validate IRI
// TODO extends typed entity that stores entity and handles get/sets, also apply to other meta data
// entities
@SuppressWarnings("unused")
public class Tag extends StaticEntity {
  public Tag(Entity entity) {
    super(entity);
  }

  public Tag(EntityType entityType) {
    super(entityType);
  }

  public Tag(String identifier, EntityType entityType) {
    super(entityType);
    setId(identifier);
  }

  public static Tag newInstance(Tag tag) {
    Tag tagCopy = new Tag(tag.getEntityType());
    tagCopy.setId(tag.getId());
    tagCopy.setObjectIri(tag.getObjectIri());
    tagCopy.setLabel(tag.getLabel());
    tagCopy.setRelationIri(tag.getRelationIri());
    tagCopy.setRelationLabel(tag.getRelationLabel());
    tagCopy.setCodeSystem(tag.getCodeSystem());
    return tagCopy;
  }

  public boolean equals(IRI relation, IRI object) {
    return relation.toString().equals(getRelationIri()) && object.toString().equals(getObjectIri());
  }

  public String getId() {
    return getString(TagMetadata.ID);
  }

  public Tag setId(String identifier) {
    set(TagMetadata.ID, identifier);
    return this;
  }

  @Nullable
  @CheckForNull
  public String getObjectIri() {
    return getString(TagMetadata.OBJECT_IRI);
  }

  public Tag setObjectIri(String objectIri) {
    set(TagMetadata.OBJECT_IRI, objectIri);
    return this;
  }

  public String getLabel() {
    return getString(TagMetadata.LABEL);
  }

  public Tag setLabel(String label) {
    set(TagMetadata.LABEL, label);
    return this;
  }

  public String getRelationIri() {
    return getString(TagMetadata.RELATION_IRI);
  }

  public Tag setRelationIri(String relationIri) {
    set(TagMetadata.RELATION_IRI, relationIri);
    return this;
  }

  public String getRelationLabel() {
    return getString(TagMetadata.RELATION_LABEL);
  }

  public Tag setRelationLabel(String relationLabel) {
    set(TagMetadata.RELATION_LABEL, relationLabel);
    return this;
  }

  @Nullable
  @CheckForNull
  public String getValue() {
    return getString(TagMetadata.VALUE);
  }

  public void setValue(String value) {
    set(TagMetadata.VALUE, value);
  }

  @Nullable
  @CheckForNull
  public String getCodeSystem() {
    return getString(TagMetadata.CODE_SYSTEM);
  }

  public Tag setCodeSystem(String codeSystem) {
    set(TagMetadata.CODE_SYSTEM, codeSystem);
    return this;
  }

  /**
   * Based on generated AutoValue class:
   *
   * <pre><code>
   * {@literal @}AutoValue
   * public abstract class Tag
   * {
   *     public abstract String getId();
   *    {@literal @}Nullable public abstract String getObjectIri();
   *     public abstract String getLabel();
   *     public abstract String getRelationIri();
   *     public abstract String getRelationLabel();
   *    {@literal @}Nullable public abstract String getCodeSystem();
   * }
   * </code></pre>
   */
  @SuppressWarnings("java:S2259") // potential multi-threading NPEs
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof Tag) {
      Tag that = (Tag) o;
      return (this.getId().equals(that.getId()))
          && ((this.getObjectIri() == null)
              ? (that.getObjectIri() == null)
              : this.getObjectIri().equals(that.getObjectIri()))
          && (this.getLabel().equals(that.getLabel()))
          && (this.getRelationIri().equals(that.getRelationIri()))
          && (this.getRelationLabel().equals(that.getRelationLabel()))
          && ((this.getCodeSystem() == null)
              ? (that.getCodeSystem() == null)
              : this.getCodeSystem().equals(that.getCodeSystem()));
    }
    return false;
  }

  /**
   * Based on generated AutoValue class:
   *
   * <pre><code>
   * {@literal @}AutoValue
   * public abstract class Tag
   * {
   *     public abstract String getId();
   * }
   * </code></pre>
   */
  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= this.getId().hashCode();
    return h;
  }
}
