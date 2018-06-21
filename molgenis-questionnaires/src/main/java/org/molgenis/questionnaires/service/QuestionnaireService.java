package org.molgenis.questionnaires.service;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.response.QuestionnaireResponse;

import java.util.List;
import java.util.stream.Stream;

public interface QuestionnaireService
{
	/**
	 * Return a list of all questionnaires.
	 * Checks current user progress to set status.
	 * <p>
	 * If user does not have a data entry for a questionnaire, status is set to 'NOT_STARTED'.
	 *
	 * @return A List of {@link QuestionnaireResponse}
	 */
	Stream<EntityType> getQuestionnaires();

	/**
	 * Start a questionnaire based on ID.
	 * If current user does not have a row for the specified questionnaire, one is created.
	 * <p>
	 * Created questionnaire entries get the status 'OPEN'.
	 *
	 * @param entityTypeId The ID of a questionnaire type
	 *
	 * @return QuestionnaireResponse to return the added questionaire to the frontend
	 */
	QuestionnaireResponse startQuestionnaire(String entityTypeId);

	/**
	 * Retrieve static content for a specific questionnaire containing a "Thank you" text which is shown
	 * on submission.
	 * <p>
	 * If no static content is specified, will return a text with HTML contents by default.
	 *
	 * @param id The ID of a questionnaire
	 * @return A piece of text which can be shown after submission of a questionnaire
	 */
	String getQuestionnaireSubmissionText(String id);

	/**
	 * Find the single row in the Questionnaire table that belongs to the current user.
	 * Returns null if no row is found, or the questionnaire ID does not exist.
	 *
	 * @param entityTypeId The ID of a questionnaire table
	 * @return An {@link Entity} of type {@link Questionnaire} or Null
	 */
	Questionnaire findQuestionnaireEntity(String entityTypeId);
}
