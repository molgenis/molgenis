package org.molgenis.ontology.sorta.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.ontology.core.meta.OntologyTermMetadata;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.sorta.job.SortaJobExecution;
import org.molgenis.ontology.sorta.job.SortaJobExecutionFactory;
import org.molgenis.ontology.sorta.meta.MatchingTaskContentMetaData;
import org.molgenis.ontology.sorta.meta.SortaJobExecutionMetadata;
import org.molgenis.ontology.sorta.service.SortaService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.ui.Model;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.*;
import static org.molgenis.ontology.sorta.meta.SortaJobExecutionMetadata.SORTA_JOB_EXECUTION;

class SortaControllerTest extends AbstractMockitoTest {

  public static final String SORTA_JOB_EXECUTION_ID = "sortaJobExecutionId";
  public static final String USERNAME = "username";

  @Mock private OntologyService ontologyService;
  @Mock private SortaService sortaService;

  @Mock(answer = RETURNS_DEEP_STUBS)
  private DataService dataService;

  @Mock private UserAccountService userAccountService;
  @Mock private FileStore fileStore;
  @Mock private UserPermissionEvaluator permissionService;
  @Mock private MenuReaderService menuReaderService;
  @Mock private IdGenerator idGenerator;

  @SuppressWarnings("deprecation")
  @Mock
  private PermissionSystemService permissionSystemService;

  @Mock private MatchingTaskContentMetaData matchingTaskContentMetaData;
  @Mock private SortaJobExecutionMetadata sortaJobExecutionMetaData;
  @Mock private OntologyTermMetadata ontologyTermMetadata;
  @Mock private SortaJobExecutionFactory sortaJobExecutionFactory;
  @Mock private EntityTypeFactory entityTypeFactory;
  @Mock private AttributeFactory attrMetaFactory;
  @Mock private JobExecutor jobExecutor;

  private SortaController sortaController;
  private User user;
  private Model model;

  @BeforeEach
  void setUpBeforeMethod() {
    user = mock(User.class);
    model = mock(Model.class);
    sortaController =
        new SortaController(
            ontologyService,
            sortaService,
            userAccountService,
            fileStore,
            permissionService,
            dataService,
            menuReaderService,
            idGenerator,
            permissionSystemService,
            matchingTaskContentMetaData,
            sortaJobExecutionMetaData,
            ontologyTermMetadata,
            sortaJobExecutionFactory,
            entityTypeFactory,
            attrMetaFactory,
            jobExecutor);
  }

  @AfterEach
  void cleanUp() {
    verifyNoMoreInteractions(
        matchingTaskContentMetaData,
        sortaJobExecutionMetaData,
        ontologyTermMetadata,
        sortaJobExecutionFactory,
        entityTypeFactory,
        attrMetaFactory,
        jobExecutor,
        user);
  }

  @Test
  void testInitNullCheck() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SortaController(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null));
  }

  @Test
  void testInit() {
    setupUser();
    when(user.getUsername()).thenReturn(USERNAME).getMock();
    SortaJobExecution sortaJobExecution = setupGetJobsForCurrentUser();

    String result = sortaController.init(model);
    assertEquals("sorta-match-view", result);
    verify(model).addAttribute("existingTasks", singletonList(sortaJobExecution));
  }

  @Test
  void testGetJobs() {
    setupUser();
    when(user.getUsername()).thenReturn(USERNAME).getMock();
    SortaJobExecution sortaJobExecution = setupGetJobsForCurrentUser();

    List<SortaJobExecution> result = sortaController.getJobs();
    assertEquals(singletonList(sortaJobExecution), result);
  }

  @Test
  void testMatchTask() {
    List<Ontology> ontologies = singletonList(mock(Ontology.class));
    when(ontologyService.getOntologies()).thenReturn(ontologies);

    String result = sortaController.matchTask(model);
    assertEquals("sorta-match-view", result);
    verify(model).addAttribute("ontologies", ontologies);
  }

  @Test
  void testUpdateThreshold() {
    String threshold = "37";
    setupUser();
    when(user.isSuperuser()).thenReturn(true);
    SortaJobExecution sortaJobExecution = setupFindSortaJobExecution();

    String result = sortaController.updateThreshold(threshold, SORTA_JOB_EXECUTION_ID, model);

    String expectedResult = "sorta-match-view";
    assertEquals(expectedResult, result);
    verify(sortaJobExecution).setThreshold(37);
    verify(dataService).update(SORTA_JOB_EXECUTION, sortaJobExecution);
  }

  @Test
  void testUpdateThresholdEmptyThreshold() {}

  @Test
  void testUpdateThresholdThrowsNumberFormatException() {}

  @Test
  void testUpdateThresholdThrowsOtherException() {}

  @Test
  void testUpdateThresholdInvalidUser() {}

  @Test
  public void testMatchResult() {
    SortaJobExecution sortaJobExecution = setupFindSortaJobExecution();
    String ontologyIri = "ontologyIri";
    when(sortaJobExecution.getIdentifier()).thenReturn(SORTA_JOB_EXECUTION_ID);
    when(sortaJobExecution.getThreshold()).thenReturn(37d);
    when(sortaJobExecution.getOntologyIri()).thenReturn(ontologyIri);

    String result = sortaController.matchResult(SORTA_JOB_EXECUTION_ID, model);

    String expectedResult = "sorta-match-view";
    assertEquals(expectedResult, result);
    verify(model).addAttribute("sortaJobExecutionId", SORTA_JOB_EXECUTION_ID);
    verify(model).addAttribute("threshold", 37d);
    verify(model).addAttribute("ontologyIri", ontologyIri);
    verify(model).addAttribute("numberOfMatched", 0L);
    verify(model).addAttribute("numberOfUnmatched", 0L);
  }

  @Test
  public void testMatchResultFailure() {
    setupDontFindSortaJobExecution();
    setupGetJobsForCurrentUser();
    setupUser();
    when(user.getUsername()).thenReturn(USERNAME).getMock();
    SortaJobExecution sortaJobExecution = setupGetJobsForCurrentUser();

    String result = sortaController.matchResult(SORTA_JOB_EXECUTION_ID, model);

    assertEquals("sorta-match-view", result);
    verify(model).addAttribute("message", "Job execution not found.");
    verify(model).addAttribute("existingTasks", singletonList(sortaJobExecution));
  }

  private SortaJobExecution setupGetJobsForCurrentUser() {
    SortaJobExecution sortaJobExecution = mock(SortaJobExecution.class);
    when(dataService
            .query("sys_job_SortaJobExecution", SortaJobExecution.class)
            .eq("user", USERNAME)
            .findAll())
        .thenReturn(Stream.of(sortaJobExecution));
    return sortaJobExecution;
  }

  private void setupUser() {
    when(userAccountService.getCurrentUser()).thenReturn(user);
  }

  private SortaJobExecution setupFindSortaJobExecution() {
    SortaJobExecution sortaJobExecution = mock(SortaJobExecution.class);
    when(sortaJobExecutionMetaData.getAtomicAttributes()).thenReturn(emptyList());
    when(dataService.findOneById(any(), any(), any(), any())).thenReturn(sortaJobExecution);
    return sortaJobExecution;
  }

  private void setupDontFindSortaJobExecution() {
    when(sortaJobExecutionMetaData.getAtomicAttributes()).thenReturn(emptyList());
    when(dataService.findOneById(any(), any(), any(), any())).thenReturn(null);
  }
}
