package org.molgenis.questionnaires.service;

import org.molgenis.questionnaires.response.QuestionnaireResponse;

import java.util.List;

public interface QuestionnaireService
{
	/**
	 * Return a list of all questionnaires
	 * Creates a questionnaire entry for the current user if it does not yet exist
	 *
	 * @return A List of {@link QuestionnaireResponse}
	 */
	List<QuestionnaireResponse> getQuestionnaires();

	/**
	 * Start a questionnaire based on ID
	 * Sets the questionnaire status to 'OPEN'
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
