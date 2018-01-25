package org.molgenis.questionnaires.service.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.questionnaires.QuestionnaireResponse;
import org.molgenis.questionnaires.QuestionnaireStatus;
import org.molgenis.questionnaires.service.QuestionnaireService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.security.owned.OwnedEntityType.OWNER_USERNAME;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.i18n.LanguageService.getCurrentUserLanguageCode;
import static org.molgenis.questionnaires.QuestionnaireMetaData.ATTR_STATUS;
import static org.molgenis.questionnaires.QuestionnaireStatus.NOT_STARTED;
import static org.molgenis.questionnaires.QuestionnaireStatus.OPEN;
import static org.molgenis.questionnaires.QuestionnaireUtils.findQuestionnairesMetaData;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.*;

@Component
public class QuestionnaireServiceImpl implements QuestionnaireService
{
	private final DataService dataService;
	private final EntityManager entityManager;

	public QuestionnaireServiceImpl(DataService dataService, EntityManager entityManager)
	{
		this.dataService = Objects.requireNonNull(dataService);
		this.entityManager = requireNonNull(entityManager);
	}

	@Override
	public List<QuestionnaireResponse> getQuestionnaires()
	{
		List<QuestionnaireResponse> questionnaires;
		List<EntityType> questionnaireMeta = runAsSystem(
				() -> findQuestionnairesMetaData(dataService).collect(toList()));

		questionnaires = questionnaireMeta.stream()
										  .map(EntityType::getId)
										  .filter(name -> currentUserIsSu() || currentUserHasRole(
												  AUTHORITY_ENTITY_WRITE_PREFIX + name))
										  .map(name -> {
											  // Create entity if not yet exists for current user
											  EntityType entityType = dataService.getMeta().getEntityType(name);
											  Entity entity = findQuestionnaireEntity(name);
											  if (entity == null)
											  {
												  entity = createQuestionnaireEntity(entityType, NOT_STARTED, name);
											  }

											  return toQuestionnaireModel(entity, entityType);
										  })
										  .collect(toList());
		return questionnaires;
	}

	@Override
	public QuestionnaireResponse getQuestionnare(String name)
	{
		EntityType entityType = dataService.getMeta().getEntityType(name);

		// Once we showed the questionnaire it's status is 'OPEN'
		Entity entity = findQuestionnaireEntity(name);
		if (entity == null)
		{
			entity = createQuestionnaireEntity(entityType, OPEN, name);
		}
		else if (entity.getString(ATTR_STATUS).equals(NOT_STARTED.toString()))
		{
			entity.set(ATTR_STATUS, OPEN.toString());
			dataService.update(name, entity);
		}

		return toQuestionnaireModel(entity, entityType);
	}

	private QuestionnaireResponse toQuestionnaireModel(Entity entity, EntityType entityType)
	{
		QuestionnaireStatus status = QuestionnaireStatus.valueOf(entity.getString(ATTR_STATUS));
		return QuestionnaireResponse.create(entityType.getId(), entityType.getLabel(getCurrentUserLanguageCode()),
				entityType.getDescription(getCurrentUserLanguageCode()), status, entity.getIdValue());
	}

	private synchronized Entity createQuestionnaireEntity(EntityType entityType, QuestionnaireStatus status,
			String name)
	{
		Entity entity = findQuestionnaireEntity(name);
		if (entity == null)
		{
			entity = entityManager.create(entityType, POPULATE);
			entity.set(OWNER_USERNAME, getCurrentUsername());
			entity.set(ATTR_STATUS, status.toString());
			dataService.add(entityType.getId(), entity);
		}
		return entity;
	}

	private Entity findQuestionnaireEntity(String name)
	{
		return dataService.findOne(name, EQ(OWNER_USERNAME, getCurrentUsername()));
	}
}
