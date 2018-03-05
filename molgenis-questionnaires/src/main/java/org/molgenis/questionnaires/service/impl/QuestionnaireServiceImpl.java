package org.molgenis.questionnaires.service.impl;

import org.molgenis.core.ui.controller.StaticContentService;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireFactory;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.QuestionnaireService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.security.owned.OwnedEntityType.OWNER_USERNAME;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.QUESTIONNAIRE;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.NOT_STARTED;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.OPEN;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@Service
public class QuestionnaireServiceImpl implements QuestionnaireService
{
	private static final String DEFAULT_SUBMISSION_TEXT = "<h3>Thank you for submitting the questionnaire.</h3>";

	private final DataService dataService;
	private final EntityManager entityManager;
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final QuestionnaireFactory questionnaireFactory;
	private final StaticContentService staticContentService;

	public QuestionnaireServiceImpl(DataService dataService, EntityManager entityManager,
			UserPermissionEvaluator userPermissionEvaluator, QuestionnaireFactory questionnaireFactory,
			StaticContentService staticContentService)
	{
		this.dataService = Objects.requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.questionnaireFactory = requireNonNull(questionnaireFactory);
		this.staticContentService = requireNonNull(staticContentService);
	}

	@Override
	public List<QuestionnaireResponse> getQuestionnaires()
	{
		return dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)
						  .eq(EntityTypeMetadata.EXTENDS, QUESTIONNAIRE)
						  .findAll()
						  .filter(entityType -> userPermissionEvaluator.hasPermission(
								  new EntityTypeIdentity(entityType.getId()), EntityTypePermission.WRITE))
						  .map(entityType ->
						  {
							  String entityTypeId = entityType.getId();
							  Questionnaire questionnaireEntity = findQuestionnaireEntity(entityTypeId);

							  if (questionnaireEntity == null)
							  {
								  // Create a questionnaire entity for the current user
								  Questionnaire questionnaire = questionnaireFactory.create(
										  entityManager.create(entityType, POPULATE));

								  questionnaire.setOwner(getCurrentUsername());
								  questionnaire.setStatus(NOT_STARTED);
								  dataService.add(entityType.getId(), questionnaire);
								  return QuestionnaireResponse.create(questionnaire);
							  }
							  return QuestionnaireResponse.create(questionnaireEntity);
						  })
						  .collect(toList());
	}

	@Override
	public void startQuestionnaire(String id)
	{
		Questionnaire questionnaire = findQuestionnaireEntity(id);
		if (questionnaire.getStatus().equals(NOT_STARTED))
		{
			// Set questionnaire status to open once it has been requested
			questionnaire.setStatus(OPEN);
			dataService.update(id, questionnaire);
		}
	}

	@Override
	public String getQuestionnaireSubmissionText(String id)
	{
		String key = id + "_submissionText";
		String submissionText = staticContentService.getContent(key);

		if (submissionText == null)
		{
			submissionText = DEFAULT_SUBMISSION_TEXT;
			staticContentService.submitContent(key, submissionText);
		}

		return submissionText;
	}

	/**
	 * Find 1 row in the Questionnaire table that belongs to the current user
	 *
	 * @param entityTypeId The ID of a questionnaire table
	 * @return An {@link Entity} of type {@link Questionnaire}
	 */
	private Questionnaire findQuestionnaireEntity(String entityTypeId)
	{
		return questionnaireFactory.create(dataService.findOne(entityTypeId, EQ(OWNER_USERNAME, getCurrentUsername())));
	}
}
