package org.molgenis.data.file.processor;

import java.util.List;
import java.util.Objects;

public abstract class AbstractCellProcessor implements CellProcessor {
  private static final long serialVersionUID = 1L;

  private final boolean processHeader;
  private final boolean processData;

  public AbstractCellProcessor() {
    this(true, true);
  }

  public AbstractCellProcessor(boolean processHeader, boolean processData) {
    this.processHeader = processHeader;
    this.processData = processData;
  }

  @Override
  public boolean processHeader() {
    return this.processHeader;
  }

  @Override
  public boolean processData() {
    return this.processData;
  }

  public static String processCell(
      String value, boolean isHeader, List<CellProcessor> cellProcessors) {
    if (cellProcessors != null) {
      for (CellProcessor cellProcessor : cellProcessors) {
        boolean process =
            (isHeader && cellProcessor.processHeader())
                || (!isHeader && cellProcessor.processData());
        if (process) value = cellProcessor.process(value);
      }
    }
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractCellProcessor)) {
      return false;
    }
    AbstractCellProcessor that = (AbstractCellProcessor) o;
    return processHeader == that.processHeader && processData == that.processData;
  }

  @Override
  public int hashCode() {
    return Objects.hash(processHeader, processData);
  }
}
