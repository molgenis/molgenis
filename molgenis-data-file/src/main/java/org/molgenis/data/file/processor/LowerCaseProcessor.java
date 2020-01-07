package org.molgenis.data.file.processor;

public class LowerCaseProcessor extends AbstractCellProcessor {
  private static final long serialVersionUID = 1L;

  public LowerCaseProcessor() {
    super();
  }

  @Override
  public String process(String value) {
    return value != null ? value.toLowerCase() : null;
  }
}
