package org.molgenis.data.rest.v2;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

/**
 * An AttributeFilter represents the value of the attrs parameter in a REST query.
 *
 * <p>The AttributeFilter allows you to specify which attributes should be fetched for each {@link
 * Entity} that is retrieved.
 *
 * <p>By default, the top level entity will always be fetched with all attributes, but you can also
 * specify a list of attribute names. Those will be added to the ID and label attributes, which will
 * always be fetched.
 *
 * <p>Referenced entities will always be fetched using ID and label, but you can also specify a list
 * of attribute names. Those will be added to the ID and label attributes, which will always be
 * fetched. You can also specify the special selector `*` which will fetch all attributes for a
 * referenced entity.
 */
class AttributeFilter implements Iterable<Entry<String, AttributeFilter>> {
  private final Map<String, AttributeFilter> attributes;
  private boolean includeAllAttrs;
  private boolean includeIdAttr;
  private boolean includeLabelAttr;
  private AttributeFilter idAttrFilter;
  private AttributeFilter labelAttrFilter;

  public AttributeFilter() {
    this.attributes = new LinkedHashMap<>();
  }

  public boolean isIncludeAllAttrs() {
    return includeAllAttrs;
  }

  /** Indicates if this filter is {@link #includeAllAttrs}, and NO other attributes are selected. */
  public boolean isStar() {
    return includeAllAttrs && (attributes == null || attributes.keySet().isEmpty());
  }

  AttributeFilter setIncludeAllAttrs(boolean includeAllAttrs) {
    this.includeAllAttrs = includeAllAttrs;
    return this;
  }

  public boolean isIncludeIdAttr() {
    return includeIdAttr;
  }

  public AttributeFilter setIncludeIdAttr(boolean includeIdAttr) {
    return setIncludeIdAttr(includeIdAttr, null);
  }

  public AttributeFilter setIncludeIdAttr(boolean includeIdAttr, AttributeFilter idAttrFilter) {
    this.includeIdAttr = includeIdAttr;
    this.idAttrFilter = idAttrFilter;
    return this;
  }

  public boolean isIncludeLabelAttr() {
    return includeLabelAttr;
  }

  public AttributeFilter setIncludeLabelAttr(boolean includeLabelAttr) {
    return setIncludeLabelAttr(includeLabelAttr, null);
  }

  public AttributeFilter setIncludeLabelAttr(
      boolean includeLabelAttr, AttributeFilter labelAttrFilter) {
    this.includeLabelAttr = includeLabelAttr;
    this.labelAttrFilter = labelAttrFilter;
    return this;
  }

  public AttributeFilter getAttributeFilter(EntityType entityType, Attribute attr) {
    if (idAttrFilter != null && attr.equals(entityType.getIdAttribute())) {
      return idAttrFilter;
    } else if (labelAttrFilter != null && attr.equals(entityType.getLabelAttribute())) {
      return labelAttrFilter;
    } else {
      return attributes.get(normalize(attr.getName()));
    }
  }

  @Override
  public Iterator<Entry<String, AttributeFilter>> iterator() {
    return Collections.unmodifiableMap(attributes).entrySet().iterator();
  }

  public AttributeFilter add(String name) {
    return add(name, null);
  }

  public AttributeFilter add(String name, AttributeFilter attributeSelection) {
    attributes.put(normalize(name), attributeSelection);
    return this;
  }

  private String normalize(String name) {
    return name; // .toLowerCase();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributeFilter)) {
      return false;
    }
    AttributeFilter entries = (AttributeFilter) o;
    return isIncludeAllAttrs() == entries.isIncludeAllAttrs()
        && isIncludeIdAttr() == entries.isIncludeIdAttr()
        && isIncludeLabelAttr() == entries.isIncludeLabelAttr()
        && Objects.equals(attributes, entries.attributes)
        && Objects.equals(idAttrFilter, entries.idAttrFilter)
        && Objects.equals(labelAttrFilter, entries.labelAttrFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        attributes,
        isIncludeAllAttrs(),
        isIncludeIdAttr(),
        isIncludeLabelAttr(),
        idAttrFilter,
        labelAttrFilter);
  }

  @Override
  public String toString() {
    return "AttributeFilter [attributes="
        + attributes
        + ", includeAllAttrs="
        + includeAllAttrs
        + "]";
  }
}
