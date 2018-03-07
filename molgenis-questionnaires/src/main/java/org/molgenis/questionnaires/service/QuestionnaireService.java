package org.molgenis.questionnaires.service;

import org.molgenis.questionnaires.response.QuestionnaireResponse;

import java.util.List;

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
	List<QuestionnaireResponse> getQuestionnaires();

	/**
	 * Start a questionnaire based on ID.
	 * If current user does not have a row for the specified questionnaire, one is created.
	 * <p>
	 * Created questionnaire entries get the status 'OPEN'.
	 *
	 * @param id The ID of a questionnaire
	 */
	void startQuestionnaire(String id);

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
}
