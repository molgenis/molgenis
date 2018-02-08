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
	 * Retrieve a Questionnaire by id
	 * Sets the questionnaire status to 'OPEN' when it is retrieved for the first time
	 *
	 * @param id The ID of a questionnaire
	 * @return A {@link QuestionnaireResponse}
	 */
	QuestionnaireResponse getQuestionnaire(String id);

	/**
	 * Retrieve static content for a specific questionnaire containing a "Thank you" text which is shown
	 * on submission
	 *
	 * @param id The ID of a questionnaire
	 * @return A piece of text which can be shown after submission of a questionnaire
	 */
	String getQuestionnaireSubmissionText(String id);
}
