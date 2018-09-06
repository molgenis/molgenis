package org.molgenis.questionnaires.service;

import static org.mockito.Mockito.*;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.support.QueryImpl.EQ;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.OWNER_USERNAME;
import static org.molgenis.questionnaires.meta.QuestionnaireMetaData.QUESTIONNAIRE;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.molgenis.questionnaires.exception.QuestionnaireNotRowLevelSecuredException;
import org.molgenis.questionnaires.meta.Questionnaire;
import org.molgenis.questionnaires.meta.QuestionnaireFactory;
import org.molgenis.questionnaires.meta.QuestionnaireStatus;
import org.molgenis.questionnaires.response.QuestionnaireResponse;
import org.molgenis.questionnaires.service.impl.QuestionnaireServiceImpl;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class QuestionnaireServiceTest extends AbstractMockitoTest {
  @Mock private DataService dataService;

  @Mock private EntityManager entityManager;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @Mock private QuestionnaireFactory questionnaireFactory;

  @Mock private StaticContentService staticContentService;

  @Mock private MutableAclClassService mutableAclClassService;

  @Mock private Query<EntityType> typedQuery;

  @Mock private Query<EntityType> query;

  private QuestionnaireService questionnaireService;

  private static final String QUESTIONNAIRE_ID = "test_quest";

  @BeforeMethod
  public void setupBeforeClass() {
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

  @AfterClass
  public void afterClass() {
    MessageSourceHolder.setMessageSource(null);
  }

  @Test
  public void testGetQuestionnaires() {
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
    assertEquals(questionnaires, entityTypes);
  }

  @Test
  public void testStartQuestionnaire() {
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
    doReturn(questionnaire).when(dataService).findOne(QUESTIONNAIRE_ID, EQ(OWNER_USERNAME, null));

    // =========== Test ===========
    QuestionnaireResponse actual = questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
    assertEquals(actual.getId(), QUESTIONNAIRE_ID);
    verify(dataService).add(QUESTIONNAIRE_ID, questionnaire);
  }

  @Test(expectedExceptions = QuestionnaireNotRowLevelSecuredException.class)
  public void testStartQuestionnaireNoRowLevelSecurity() {
    // =========== Setup ===========
    EntityType entityType = mock(EntityType.class);

    when(mutableAclClassService.getAclClassTypes()).thenReturn(Collections.emptyList());
    when(entityType.getId()).thenReturn(QUESTIONNAIRE_ID);
    when(dataService.getEntityType(QUESTIONNAIRE_ID)).thenReturn(entityType);

    // =========== Test ===========
    questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
  }

  @Test
  public void testStartQuestionnaireAlreadyOpen() {
    // =========== Setup ===========
    Entity entity = mock(Entity.class);
    when(dataService.findOne(QUESTIONNAIRE_ID, EQ(OWNER_USERNAME, null))).thenReturn(entity);

    Questionnaire questionnaire = mock(Questionnaire.class);
    when(questionnaire.getIdValue()).thenReturn(QUESTIONNAIRE_ID);
    when(questionnaire.getLabel()).thenReturn("label");
    when(questionnaire.getDescription()).thenReturn("Description");
    when(questionnaire.getStatus()).thenReturn(QuestionnaireStatus.NOT_STARTED);
    when(questionnaireFactory.create(entity)).thenReturn(questionnaire);

    // =========== Test ===========
    QuestionnaireResponse actual = questionnaireService.startQuestionnaire(QUESTIONNAIRE_ID);
    assertEquals(actual.getId(), QUESTIONNAIRE_ID);
    verify(dataService, times(0)).add(QUESTIONNAIRE_ID, questionnaire);
  }

  @Test
  public void testGetQuestionnaireSubmissionTextDefault() {
    String key = QUESTIONNAIRE_ID + "_submissionText";
    when(staticContentService.getContent(key)).thenReturn(null);

    String actual = questionnaireService.getQuestionnaireSubmissionText(QUESTIONNAIRE_ID);
    String expected = "<h3>Thank you for submitting the questionnaire.</h3>";

    assertEquals(actual, expected);
    verify(staticContentService)
        .submitContent(key, "<h3>Thank you for submitting the questionnaire.</h3>");
  }

  @Test
  public void testGetQuestionnaireSubmissionText() {
    String key = QUESTIONNAIRE_ID + "_submissionText";
    when(staticContentService.getContent(key)).thenReturn("My awesome submission text");

    String actual = questionnaireService.getQuestionnaireSubmissionText(QUESTIONNAIRE_ID);
    String expected = "My awesome submission text";

    assertEquals(actual, expected);
  }
}
