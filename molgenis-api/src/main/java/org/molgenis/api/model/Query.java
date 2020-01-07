package org.molgenis.api.model;

import com.google.auto.value.AutoValue;
import java.io.Serializable;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.UnexpectedEnumException;

@AutoValue
public abstract class Query implements Serializable {
  private static final long serialVersionUID = 1L;

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
    return (String) getValue();
  }

  @SuppressWarnings("unchecked")
  public List<String> getStringListValue() {
    return (List<String>) getValue();
  }

  @SuppressWarnings("unchecked")
  public List<Query> getQueryListValue() {
    return (List<Query>) getValue();
  }

  @SuppressWarnings("unused")
  public static Query create(String newItem, Operator newOperator, Object newValue) {
    return builder().setItem(newItem).setOperator(newOperator).setValue(newValue).build();
  }

  public static Builder builder() {
    return new AutoValue_Query.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setItem(String newItem);

    public abstract Builder setOperator(Operator newOperator);

    public abstract Builder setValue(Object newValue);

    abstract Query autoBuild();

    public Query build() {
      Query query = autoBuild();
      Operator operator = query.getOperator();
      switch (operator) {
        case EQUALS:
        case NOT_EQUALS:
        case CONTAINS:
        case LESS_THAN:
        case LESS_THAN_OR_EQUAL_TO:
        case GREATER_THAN:
        case GREATER_THAN_OR_EQUAL_TO:
          validateItemStringOperator(query);
          break;
        case MATCHES:
          validateStringOperator(query);
          break;
        case IN:
        case NOT_IN:
          validateStringListOperator(query);
          break;
        case AND:
        case OR:
          validateQueryListOperator(query);
          break;
        default:
          throw new UnexpectedEnumException(operator);
      }
      return query;
    }

    private void validateItemStringOperator(Query query) {
      if (query.getItem() == null) {
        throw new IllegalStateException("query selector cannot be empty");
      }
      validateStringOperator(query);
    }

    private void validateStringOperator(Query query) {
      Object stringValue = query.getValue();
      if (stringValue != null && !(stringValue instanceof String)) {
        throw new IllegalStateException("query value must be of type String");
      }
    }

    private void validateStringListOperator(Query query) {
      if (query.getItem() == null) {
        throw new IllegalStateException("query selector cannot be empty");
      }
      Object listValueObject = query.getValue();
      if (!(listValueObject instanceof List)) {
        throw new IllegalStateException("query value must be of type List");
      }
      for (Object listValueItem : (List) listValueObject) {
        if (listValueItem != null && !(listValueItem instanceof String)) {
          throw new IllegalStateException("query list value must be of type String");
        }
      }
    }

    private void validateQueryListOperator(Query query) {
      if (query.getItem() != null) {
        throw new IllegalStateException();
      }
      Object queryValueObject = query.getValue();
      if (!(queryValueObject instanceof List)) {
        throw new IllegalStateException("query value must be of type List");
      }
      for (Object queryValueItem : (List) queryValueObject) {
        if (!(queryValueItem instanceof Query)) {
          throw new IllegalStateException("query list value must be of type Query");
        }
      }
    }
  }
}
