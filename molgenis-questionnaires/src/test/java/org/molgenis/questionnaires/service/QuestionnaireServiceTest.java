package org.molgenis.questionnaires.service;

import org.mockito.Mock;
import org.molgenis.core.ui.controller.StaticContentService;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireFactory;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.impl.QuestionnaireServiceImpl;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.security.EntityTypePermission.WRITE;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.OWNER_USERNAME;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.QUESTIONNAIRE;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.NOT_STARTED;
import static org.molgenis.questionnaires.meta.QuestionnaireStatus.OPEN;
import static org.testng.Assert.assertEquals;

public class QuestionnaireServiceTest
{
	@Mock
	private DataService dataService;

	@Mock
	private EntityManager entityManager;

	@Mock
	private UserPermissionEvaluator userPermissionEvaluator;

	@Mock
	private QuestionnaireFactory questionnaireFactory;

	@Mock
	private StaticContentService staticContentService;

	private QuestionnaireService questionnaireService;

	private static final String QUESTIONNAIRE_ID = "test_quest";

	@BeforeClass
	public void setupBeforeClass()
	{
		initMocks(this);
		questionnaireService = new QuestionnaireServiceImpl(dataService, entityManager, userPermissionEvaluator,
				questionnaireFactory, staticContentService);
	}

	@Test
	public void testGetQuestionnaires()
	{
		// =========== Setup ===========
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(QUESTIONNAIRE_ID);
		when(entityType.getLabel()).thenReturn("label");
		when(entityType.getDescription()).thenReturn("description");

		Query<EntityType> typedQuery = mock(Query.class);
		Query<EntityType> query = mock(Query.class);
		when(typedQuery.eq(EntityTypeMetadata.EXTENDS, QUESTIONNAIRE)).thenReturn(query);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(typedQuery);

		when(query.findAll()).thenReturn(Stream.of(entityType));
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(QUESTIONNAIRE_ID), WRITE)).thenReturn(true);

		Entity entity = mock(Entity.class);
		when(dataService.findOne(QUESTIONNAIRE_ID, EQ(OWNER_USERNAME, null))).thenReturn(entity);

		Questionnaire questionnaire = mock(Questionnaire.class);
		when(questionnaire.getStatus()).thenReturn(OPEN);
		when(questionnaireFactory.create(entity)).thenReturn(questionnaire);

		// =========== Test ===========
		List<QuestionnaireResponse> actual = questionnaireService.getQuestionnaires();
		QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.create(QUESTIONNAIRE_ID, "label",
				"description", OPEN);
		List<QuestionnaireResponse> expected = newArrayList(questionnaireResponse);

		assertEquals(actual, expected);
	}

	@Test
	public void testGetQuestionnairesWithNoExistingRow()
	{
		// =========== Setup ===========
		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(QUESTIONNAIRE_ID);
		when(entityType.getLabel()).thenReturn("label");
		when(entityType.getDescription()).thenReturn("description");

		Query<EntityType> typedQuery = mock(Query.class);
		Query<EntityType> query = mock(Query.class);
		when(typedQuery.eq(EntityTypeMetadata.EXTENDS, QUESTIONNAIRE)).thenReturn(query);
		when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(typedQuery);

		when(query.findAll()).thenReturn(Stream.of(entityType));
		when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity(QUESTIONNAIRE_ID), WRITE)).thenReturn(true);

		Entity entity = null;
		when(dataService.findOne(QUESTIONNAIRE_ID, EQ(OWNER_USERNAME, null))).thenReturn(entity);
		when(questionnaireFactory.create(entity)).thenReturn(null);

		// =========== Test ===========
		List<QuestionnaireResponse> actual = questionnaireService.getQuestionnaires();
		QuestionnaireResponse questionnaireResponse = QuestionnaireResponse.create(QUESTIONNAIRE_ID, "label",
				"description", NOT_STARTED);
		List<QuestionnaireResponse> expected = newArrayList(questionnaireResponse);

		assertEquals(actual, expected);
	}

	@Test
	public void testStartQuestionnaire()
	{
		// =========== Setup ===========
		Entity entity = null;
		when(dataService.findOne(QUESTIONNAIRE_ID, EQ(OWNER_USERNAME, null))).thenReturn(entity);
		when(questionnaireFactory.create(entity)).thenReturn(null);

		EntityType entityType = mock(EntityType.class);
		when(dataService.getEntityType(QUESTIONNAIRE_ID)).thenReturn(entityType);

		Entity questionnaireEntity = mock(Entity.class);
		when(entityManager.create(entityType, POPULATE)).thenReturn(questionnaireEntity);

		Questionnaire questionnaire = mock(Questionnaire.class);
		when(questionnaireFactory.create(questionnaireEntity)).thenReturn(questionnaire);

		// =========== Test ===========
		questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
		verify(dataService).add(QUESTIONNAIRE_ID, questionnaire);
	}

	@Test
	public void testStartQuestionnaireAlreadyOpen()
	{
		// =========== Setup ===========
		Entity entity = mock(Entity.class);
		when(dataService.findOne(QUESTIONNAIRE_ID, EQ(OWNER_USERNAME, null))).thenReturn(entity);

		Questionnaire questionnaire = mock(Questionnaire.class);
		when(questionnaireFactory.create(entity)).thenReturn(questionnaire);

		// =========== Test ===========
		questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
		verify(dataService, times(0)).add(QUESTIONNAIRE_ID, questionnaire);
	}

	@Test
	public void testGetQuestionnaireSubmissionTextDefault()
	{
		String key = QUESTIONNAIRE_ID + "_submissionText";
		when(staticContentService.getContent(key)).thenReturn(null);

		String actual = questionnaireService.getQuestionnaireSubmissionText(QUESTIONNAIRE_ID);
		String expected = "<h3>Thank you for submitting the questionnaire.</h3>";

		assertEquals(actual, expected);
		verify(staticContentService).submitContent(key, "<h3>Thank you for submitting the questionnaire.</h3>");
	}

	@Test
	public void testGetQuestionnaireSubmissionText()
	{
		String key = QUESTIONNAIRE_ID + "_submissionText";
		when(staticContentService.getContent(key)).thenReturn("My awesome submission text");

		String actual = questionnaireService.getQuestionnaireSubmissionText(QUESTIONNAIRE_ID);
		String expected = "My awesome submission text";

		assertEquals(actual, expected);
	}
}
