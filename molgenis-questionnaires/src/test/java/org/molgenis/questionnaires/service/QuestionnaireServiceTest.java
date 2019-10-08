package org.molgenis.questionnaires.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.OWNER_USERNAME;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.QUESTIONNAIRE;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.molgenis.core.ui.controller.StaticContentService;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.questionnaires.exception.QuestionnaireNotRowLevelSecuredException;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireFactory;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.impl.QuestionnaireServiceImpl;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.i18n.MessageSourceHolder;
import org.molgenis.util.i18n.TestAllPropertiesMessageSource;
import org.molgenis.util.i18n.format.MessageFormatFactory;

class QuestionnaireServiceTest extends AbstractMockitoTest {
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private EntityManager entityManager;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @Mock private QuestionnaireFactory questionnaireFactory;

  @Mock private StaticContentService staticContentService;

  @Mock private MutableAclClassService mutableAclClassService;

  @Mock private Query<EntityType> typedQuery;

  @Mock private Query<EntityType> query;

  private QuestionnaireService questionnaireService;

  private static final String QUESTIONNAIRE_ID = "test_quest";

  @BeforeEach
  void setupBeforeClass() {
    TestAllPropertiesMessageSource messageSource =
        new TestAllPropertiesMessageSource(new MessageFormatFactory());
    messageSource.addMolgenisNamespaces("questionnaire");
    MessageSourceHolder.setMessageSource(messageSource);

    questionnaireService =
        new QuestionnaireServiceImpl(
            dataService,
            entityManager,
            userPermissionEvaluator,
            questionnaireFactory,
            staticContentService,
            mutableAclClassService);
  }

  @AfterAll
  static void afterClass() {
    MessageSourceHolder.setMessageSource(null);
  }

  @Test
  void testGetQuestionnaires() {
    // =========== Setup ===========
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn(QUESTIONNAIRE_ID);

    when(typedQuery.eq(EntityTypeMetadata.EXTENDS, QUESTIONNAIRE)).thenReturn(query);
    when(dataService.query(ENTITY_TYPE_META_DATA, EntityType.class)).thenReturn(typedQuery);

    when(query.findAll()).thenReturn(Stream.of(entityType));
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new EntityTypeIdentity(QUESTIONNAIRE_ID), EntityTypePermission.ADD_DATA);
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new EntityTypeIdentity(QUESTIONNAIRE_ID), EntityTypePermission.UPDATE_DATA);

    // =========== Test ===========
    List<EntityType> questionnaires =
        questionnaireService.getQuestionnaires().collect(Collectors.toList());
    List<EntityType> entityTypes = Collections.singletonList(entityType);
    assertEquals(entityTypes, questionnaires);
  }

  @Test
  void testStartQuestionnaire() {
    // =========== Setup ===========
    EntityType entityType = mock(EntityType.class);
    when(mutableAclClassService.getAclClassTypes())
        .thenReturn(Collections.singletonList("entity-test_quest"));
    when(entityType.getId()).thenReturn(QUESTIONNAIRE_ID);
    when(dataService.getEntityType(QUESTIONNAIRE_ID)).thenReturn(entityType);

    Entity questionnaireEntity = mock(Entity.class);
    when(entityManager.create(entityType, POPULATE)).thenReturn(questionnaireEntity);

    Questionnaire questionnaire = mock(Questionnaire.class);
    when(questionnaire.getIdValue()).thenReturn(QUESTIONNAIRE_ID);
    when(questionnaire.getLabel()).thenReturn("label");
    when(questionnaire.getDescription()).thenReturn("Description");
    when(questionnaire.getStatus()).thenReturn(QuestionnaireStatus.NOT_STARTED);
    when(questionnaire.getEntityType()).thenReturn(entityType);

    doReturn(questionnaire).when(questionnaireFactory).create(questionnaireEntity);
    doReturn(null, questionnaire).when(questionnaireFactory).create(questionnaire);

    when(dataService.query(QUESTIONNAIRE_ID).eq(OWNER_USERNAME, null).findOne())
        .thenReturn(questionnaire);

    // =========== Test ===========
    QuestionnaireResponse actual = questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
    assertEquals(QUESTIONNAIRE_ID, actual.getId());
    verify(dataService).add(QUESTIONNAIRE_ID, questionnaire);
  }

  @Test
  void testStartQuestionnaireNoRowLevelSecurity() {
    // =========== Setup ===========
    EntityType entityType = mock(EntityType.class);

    when(mutableAclClassService.getAclClassTypes()).thenReturn(Collections.emptyList());
    when(entityType.getId()).thenReturn(QUESTIONNAIRE_ID);
    when(dataService.getEntityType(QUESTIONNAIRE_ID)).thenReturn(entityType);

    // =========== Test ===========
    assertThrows(
        QuestionnaireNotRowLevelSecuredException.class,
        () -> questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID));
  }

  @Test
  void testStartQuestionnaireAlreadyOpen() {
    // =========== Setup ===========
    Entity entity = mock(Entity.class);
    when(dataService.query(QUESTIONNAIRE_ID).eq(OWNER_USERNAME, null).findOne()).thenReturn(entity);

    Questionnaire questionnaire = mock(Questionnaire.class);
    when(questionnaire.getIdValue()).thenReturn(QUESTIONNAIRE_ID);
    when(questionnaire.getLabel()).thenReturn("label");
    when(questionnaire.getDescription()).thenReturn("Description");
    when(questionnaire.getStatus()).thenReturn(QuestionnaireStatus.NOT_STARTED);
    when(questionnaireFactory.create(entity)).thenReturn(questionnaire);

    // =========== Test ===========
    QuestionnaireResponse actual = questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
    assertEquals(QUESTIONNAIRE_ID, actual.getId());
    verify(dataService, times(0)).add(QUESTIONNAIRE_ID, questionnaire);
  }

  @Test
  void testGetQuestionnaireSubmissionTextDefault() {
    String key = QUESTIONNAIRE_ID + "_submissionText";
    when(staticContentService.getContent(key)).thenReturn(null);

    String actual = questionnaireService.getQuestionnaireSubmissionText(QUESTIONNAIRE_ID);
    String expected = "<h3>Thank you for submitting the questionnaire.</h3>";

    assertEquals(expected, actual);
    verify(staticContentService)
        .submitContent(key, "<h3>Thank you for submitting the questionnaire.</h3>");
  }

  @Test
  void testGetQuestionnaireSubmissionText() {
    String key = QUESTIONNAIRE_ID + "_submissionText";
    when(staticContentService.getContent(key)).thenReturn("My awesome submission text");

    String actual = questionnaireService.getQuestionnaireSubmissionText(QUESTIONNAIRE_ID);
    String expected = "My awesome submission text";

    assertEquals(expected, actual);
  }
}
