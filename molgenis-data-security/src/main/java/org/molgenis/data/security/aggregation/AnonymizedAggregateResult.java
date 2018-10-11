package org.molgenis.data.security.aggregation;

import java.util.List;
import java.util.Objects;
import org.molgenis.data.aggregation.AggregateResult;

public class AnonymizedAggregateResult extends AggregateResult {
  private final int anonymizationThreshold;

  public AnonymizedAggregateResult(
      List<List<Long>> matrix,
      List<Object> xLabels,
      List<Object> yLabels,
      int anonymizationThreshold) {
    super(matrix, xLabels, yLabels);
    this.anonymizationThreshold = anonymizationThreshold;
  }

  public int getAnonymizationThreshold() {
    return anonymizationThreshold;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AnonymizedAggregateResult)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    AnonymizedAggregateResult that = (AnonymizedAggregateResult) o;
    return getAnonymizationThreshold() == that.getAnonymizationThreshold();
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getAnonymizationThreshold());
  }
}
