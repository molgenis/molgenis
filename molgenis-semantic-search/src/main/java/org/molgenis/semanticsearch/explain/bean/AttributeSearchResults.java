package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.semanticsearch.semantic.Hits;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AttributeSearchResults {
  public abstract Attribute getAttribute();

  public abstract Hits<ExplainedAttribute> getHits();

  public static AttributeSearchResults create(Attribute attribute, Hits<ExplainedAttribute> hits) {
    return new AutoValue_AttributeSearchResults(attribute, hits);
  }
}
