package org.molgenis.ontology.sorta.controller;

import static java.util.Collections.singletonList;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Stream;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SortaControllerTest extends AbstractMockitoTest {

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

  @BeforeMethod
  public void setUpBeforeMethod() {
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

  @Test(expectedExceptions = NullPointerException.class)
  public void testSortaController() {
    new SortaController(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null);
  }

  @Test
  public void testInit() {
    String username = "username";
    User user = when(mock(User.class).getUsername()).thenReturn(username).getMock();
    when(userAccountService.getCurrentUser()).thenReturn(user);

    SortaJobExecution sortaJobExecution = mock(SortaJobExecution.class);
    when(dataService
            .query("sys_job_SortaJobExecution", SortaJobExecution.class)
            .eq("user", username)
            .findAll())
        .thenReturn(Stream.of(sortaJobExecution));

    Model model = mock(Model.class);
    assertEquals("sorta-match-view", sortaController.init(model));
    verify(model).addAttribute("existingTasks", singletonList(sortaJobExecution));
  }

  @Test
  public void testGetJobs() {
    String username = "username";
    User user = when(mock(User.class).getUsername()).thenReturn(username).getMock();
    when(userAccountService.getCurrentUser()).thenReturn(user);

    SortaJobExecution sortaJobExecution = mock(SortaJobExecution.class);
    when(dataService
            .query("sys_job_SortaJobExecution", SortaJobExecution.class)
            .eq("user", username)
            .findAll())
        .thenReturn(Stream.of(sortaJobExecution));
    assertEquals(sortaController.getJobs(), singletonList(sortaJobExecution));
  }

  @Test
  public void testMatchTask() {
    List<Ontology> ontologies = singletonList(mock(Ontology.class));
    when(ontologyService.getOntologies()).thenReturn(ontologies);
    Model model = mock(Model.class);
    assertEquals("sorta-match-view", sortaController.matchTask(model));
    verify(model).addAttribute("ontologies", ontologies);
  }
}
