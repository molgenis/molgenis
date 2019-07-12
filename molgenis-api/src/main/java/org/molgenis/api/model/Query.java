package org.molgenis.api.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@AutoValue
public abstract class Query {
  public enum Operator {
    /** item: not-null value: String */
    EQUALS,
    /** item: not-null value: String */
    NOT_EQUALS,
    /** item: not-null value: String List */
    IN,
    /** item: not-null value: String List */
    NOT_IN,
    /** field: null/not-null value: String */
    MATCHES,
    /** field: not-null value: String */
    CONTAINS,
    /** field: not-null value: String */
    LESS_THAN,
    /** field: not-null value: String */
    LESS_THAN_OR_EQUAL_TO,
    /** field: not-null value: String */
    GREATER_THAN,
    /** field: not-null value: String */
    GREATER_THAN_OR_EQUAL_TO,
    /** field: null value: Query List */
    AND,
    /** field: null value: Query List */
    OR
  }

  @Nullable
  @CheckForNull
  public abstract String getItem();

  public abstract Operator getOperator();

  @Nullable
  @CheckForNull
  public abstract Object getValue();

  public String getStringValue() {
    return (String) getValue(); // TODO check if operator matches
  }

  public List<String> getStringListValue() {
    return (List<String>) getValue(); // TODO check if operator matches
  }

  public List<Query> getQueryListValue() {
    return (List<Query>) getValue(); // TODO check if operator matches
  }

  public static Query create(String newItem, Operator newOperator, Object newValue) {
    return builder().setItem(newItem).setOperator(newOperator).setValue(newValue).build();
  }

  public static Builder builder() {
    // TODO assert that value type matches operator
    return new AutoValue_Query.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setItem(String newItem);

    public abstract Builder setOperator(Operator newOperator);

    public abstract Builder setValue(Object newValue);

    public abstract Query build();
  }
}
