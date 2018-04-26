package org.molgenis.core.ui.data.importer.wizard;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.importer.*;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { ImportWizardControllerTest.Config.class })
@TestExecutionListeners(listeners = WithSecurityContextTestExecutionListener.class)
public class ImportWizardControllerTest extends AbstractMockitoTestNGSpringContextTests
{
	private static final String USERNAME = "user";

	@Mock
	private UploadWizardPage uploadWizardPage;
	@Mock
	private OptionsWizardPage optionsWizardPage;
	@Mock
	private PackageWizardPage packageWizardPage;
	@Mock
	private ValidationResultWizardPage validationResultWizardPage;
	@Mock
	private ImportResultsWizardPage importResultsWizardPage;
	@Mock
	private DataService dataService;
	@Mock
	private ImportServiceFactory importServiceFactory;
	@Mock
	private FileStore fileStore;
	@Mock
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	@Mock
	private ImportRunService importRunService;
	@Mock
	private ExecutorService executorService;

	private ImportWizardController importWizardController;

	@BeforeMethod
	private void setUpBeforeMethod()
	{
		importWizardController = new ImportWizardController(uploadWizardPage, optionsWizardPage, packageWizardPage,
				validationResultWizardPage, importResultsWizardPage, dataService, importServiceFactory, fileStore,
				fileRepositoryCollectionFactory, importRunService);
		importWizardController.setExecutorService(executorService);
	}

	@DataProvider(name = "testImportFileProvider")
	public static Iterator<Object[]> testInitProvider()
	{
		return asList(new Object[] { "add", DatabaseAction.ADD },
				new Object[] { "update", DatabaseAction.UPDATE }).iterator();
	}

	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "testImportFileProvider")
	public void testImportFile(String action, DatabaseAction databaseAction) throws IOException, URISyntaxException
	{
		HttpSession httpSession = mock(HttpSession.class);
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(httpServletRequest.getSession()).thenReturn(httpSession);

		String filename = "example.xlsx";
		MultipartFile multipartFile = createMultipartFile(filename);
		String entityTypeId = null;
		String packageId = null;
		Boolean notify = null;

		String tmpFileName = "tmp-example.xlsx";
		File tmpFile = mock(File.class);
		when(tmpFile.getName()).thenReturn(tmpFileName);
		when(fileStore.store(any(), eq("example.xlsx"))).thenReturn(tmpFile);

		ImportService importService = mock(ImportService.class);
		when(importServiceFactory.getImportService(tmpFileName)).thenReturn(importService);

		String importRunEntityTypeId = "entityTypeId";
		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(importRunEntityTypeId).getMock();

		String importRunIdValue = "importRunId";
		ImportRun importRun = mock(ImportRun.class);
		when(importRun.getId()).thenReturn(importRunIdValue);
		when(importRun.getIdValue()).thenReturn(importRunIdValue);
		when(importRun.getEntityType()).thenReturn(entityType);
		when(importRunService.addImportRun(USERNAME, false)).thenReturn(importRun);

		@SuppressWarnings("ConstantConditions")
		ResponseEntity<String> responseEntity = importWizardController.importFile(httpServletRequest, multipartFile,
				entityTypeId, packageId, action, notify);
		assertEquals(responseEntity, ResponseEntity.created(new java.net.URI("/api/v2/entityTypeId/importRunId"))
												   .contentType(TEXT_PLAIN)
												   .body("/api/v2/entityTypeId/importRunId"));

		verify(fileStore).store(any(), eq(filename));
		ArgumentCaptor<ImportJob> importJobArgumentCaptor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService).execute(importJobArgumentCaptor.capture());
		assertEquals(importJobArgumentCaptor.getValue(),
				new ImportJob(importService, SecurityContextHolder.getContext(), null, databaseAction, importRunIdValue,
						importRunService, httpSession, "base"));
	}

	@Test
	public void testImportFileActionUnknown() throws IOException, URISyntaxException
	{
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

		String filename = "example.xlsx";
		MultipartFile multipartFile = createMultipartFile(filename);
		String entityTypeId = null;
		String packageId = null;
		String action = "unknownAction";
		Boolean notify = null;

		String tmpFileName = "tmp-example.xlsx";
		File tmpFile = mock(File.class);
		when(tmpFile.getName()).thenReturn(tmpFileName);
		when(fileStore.store(any(), eq("example.xlsx"))).thenReturn(tmpFile);

		@SuppressWarnings("ConstantConditions")
		ResponseEntity<String> responseEntity = importWizardController.importFile(httpServletRequest, multipartFile,
				entityTypeId, packageId, action, notify);
		assertEquals(responseEntity, ResponseEntity.badRequest()
												   .contentType(TEXT_PLAIN)
												   .body("Invalid action:[UNKNOWNACTION] valid values: [ADD, ADD_UPDATE_EXISTING, UPDATE, ADD_IGNORE_EXISTING]"));

		verify(fileStore).store(any(), eq(filename));
		verifyZeroInteractions(executorService);
	}

	private MultipartFile createMultipartFile(String filename) throws IOException
	{
		File file = new File("/src/test/resources/" + filename);

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		return new CommonsMultipartFile(fileItem);
	}

	static class Config
	{

	}
}
