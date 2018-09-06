package org.molgenis.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import java.util.Set;
import org.molgenis.data.meta.model.Attribute;

@AutoValue
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class ExplainedAttribute {
  public abstract Attribute getAttribute();

  public abstract Set<ExplainedQueryString> getExplainedQueryStrings();

  public abstract boolean isHighQuality();

  public static ExplainedAttribute create(
      Attribute attribute, Set<ExplainedQueryString> explainedQueryStrings, boolean isHighQuality) {
    return new AutoValue_ExplainedAttribute(attribute, explainedQueryStrings, isHighQuality);
  }
}
