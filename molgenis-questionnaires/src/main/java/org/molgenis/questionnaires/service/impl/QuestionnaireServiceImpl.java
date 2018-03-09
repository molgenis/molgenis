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
import org.molgenis.questionnaires.meta.QuestionnaireStatus;
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
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.OWNER_USERNAME;
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
						  .map(this::createQuestionnaireResponse)
						  .collect(toList());
	}

	@Override
	public void startQuestionnaire(String id)
	{
		Questionnaire questionnaire = findQuestionnaireEntity(id);
		if (questionnaire == null)
		{
			EntityType questionnaireEntityType = dataService.getEntityType(id);
			questionnaire = questionnaireFactory.create(entityManager.create(questionnaireEntityType, POPULATE));
			questionnaire.setOwner(getCurrentUsername());
			questionnaire.setStatus(OPEN);
			dataService.add(id, questionnaire);
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
	 * Create a {@link QuestionnaireResponse} based on an {@link EntityType}
	 * Will set status to {@link QuestionnaireStatus}.OPEN if there is a data entry for the current user.
	 *
	 * @param entityType A Questionnaire EntityType
	 * @return A {@link QuestionnaireResponse}
	 */
	private QuestionnaireResponse createQuestionnaireResponse(EntityType entityType)
	{
		String entityTypeId = entityType.getId();

		QuestionnaireStatus status = NOT_STARTED;
		Questionnaire questionnaireEntity = findQuestionnaireEntity(entityTypeId);
		if (questionnaireEntity != null)
		{
			status = questionnaireEntity.getStatus();
		}
		return QuestionnaireResponse.create(entityTypeId, entityType.getLabel(), entityType.getDescription(), status);
	}

	/**
	 * Find 1 row in the Questionnaire table that belongs to the current user.
	 * Returns null if no row is found, or the questionnaire ID does not exist.
	 *
	 * @param entityTypeId The ID of a questionnaire table
	 * @return An {@link Entity} of type {@link Questionnaire}
	 */
	private Questionnaire findQuestionnaireEntity(String entityTypeId)
	{
		return questionnaireFactory.create(dataService.findOne(entityTypeId, EQ(OWNER_USERNAME, getCurrentUsername())));
	}
}
