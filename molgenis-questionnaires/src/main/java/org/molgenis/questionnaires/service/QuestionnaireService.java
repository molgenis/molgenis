package org.molgenis.questionnaires.service;

import org.molgenis.questionnaires.response.QuestionnaireResponse;

import java.util.List;

public interface QuestionnaireService
{
	/**
	 * Return a list of all questionnaires
	 * Creates a questionnaire entry for the current user if it does not yet exist
	 */
	List<QuestionnaireResponse> getQuestionnaires();

	/**
	 * Retrieve a Questionnaire by name
	 * Sets the questionnaire status to 'OPEN' when it is retrieved for the first time
	 */
	QuestionnaireResponse getQuestionnaire(String name);
}
