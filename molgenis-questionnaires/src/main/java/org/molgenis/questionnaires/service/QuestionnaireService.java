package org.molgenis.questionnaires.service;

import org.molgenis.questionnaires.QuestionnaireResponse;

import java.util.List;

public interface QuestionnaireService
{
	/**
	 * Return a list of all questionnaires
	 */
	List<QuestionnaireResponse> getQuestionnaires();

	/**
	 * Retrieve a Questionnare by name
	 */
	QuestionnaireResponse getQuestionnare(String name);
}
