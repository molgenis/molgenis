package org.molgenis.questionnaires.response;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;

@AutoValue
@AutoGson(autoValueClass = AutoValue_QuestionnaireResponse.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class QuestionnaireResponse {
  public abstract String getId();

  public abstract String getLabel();

  @Nullable
  @CheckForNull
  public abstract String getDescription();

  public abstract QuestionnaireStatus getStatus();

  public static QuestionnaireResponse create(
      String id, String label, String description, QuestionnaireStatus status) {
    return new AutoValue_QuestionnaireResponse(id, label, description, status);
  }

  public static QuestionnaireResponse create(Questionnaire questionnaire) {
    return create(
        String.valueOf(questionnaire.getIdValue()),
        questionnaire.getLabel(),
        questionnaire.getDescription(),
        questionnaire.getStatus());
  }
}
