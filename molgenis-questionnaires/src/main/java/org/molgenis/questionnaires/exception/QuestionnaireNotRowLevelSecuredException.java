package org.molgenis.questionnaires.exception;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.CodedRuntimeException;

public class QuestionnaireNotRowLevelSecuredException extends CodedRuntimeException {
  private static final String ERROR_CODE = "Q01";
  private final transient EntityType questionnaireEntityType;

  public QuestionnaireNotRowLevelSecuredException(EntityType questionnaireEntityType) {
    super(ERROR_CODE);
    this.questionnaireEntityType = questionnaireEntityType;
  }

  @Override
  public String getMessage() {
    return String.format("questionaire:%s", questionnaireEntityType.getId());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {questionnaireEntityType.getId()};
  }
}
