package org.molgenis.data.importer.wizard;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.*;
import org.molgenis.data.importer.*;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.molgenis.data.importer.wizard.ImportWizardControllerTest.Config;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { Config.class })
@TestExecutionListeners(WithSecurityContextTestExecutionListener.class)
public class ImportWizardControllerTest extends AbstractMolgenisSpringTest
{
	private ImportWizardController controller;
	private WebRequest webRequest;

	@Autowired
	private DataService dataService;

	@Autowired
	private ImportRunFactory importRunFactory;

	@Autowired
	private MetaDataService metaDataService;

	private ImportServiceFactory importServiceFactory;
	private FileStore fileStore;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private ImportRunService importRunService;
	private ExecutorService executorService;
	private FileRepositoryCollection repositoryCollection;
	private ImportService importService;
	private Instant date;
	private EntityType entityType;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws ParseException
	{
		reset(dataService);
		UploadWizardPage uploadWizardPage = mock(UploadWizardPage.class);
		OptionsWizardPage optionsWizardPage = mock(OptionsWizardPage.class);
		ValidationResultWizardPage validationResultWizardPage = mock(ValidationResultWizardPage.class);
		ImportResultsWizardPage importResultsWizardPage = mock(ImportResultsWizardPage.class);
		PackageWizardPage packageWizardPage = mock(PackageWizardPage.class);
		importServiceFactory = mock(ImportServiceFactory.class);
		fileStore = mock(FileStore.class);
		fileRepositoryCollectionFactory = mock(FileRepositoryCollectionFactory.class);
		importRunService = mock(ImportRunService.class);
		executorService = mock(ExecutorService.class);
		dataService = mock(DataService.class);
		repositoryCollection = mock(FileRepositoryCollection.class);
		importService = mock(ImportService.class);
		entityType = mock(EntityType.class);

		controller = new ImportWizardController(uploadWizardPage, optionsWizardPage, packageWizardPage,
				validationResultWizardPage, importResultsWizardPage, dataService, importServiceFactory, fileStore,
				fileRepositoryCollectionFactory, importRunService, executorService);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity1,entity2");

		when(dataService.findAll(eq(EntityTypeMetadata.ENTITY_TYPE_META_DATA), any(),
				eq(new Fetch().field(EntityTypeMetadata.ID).field(EntityTypeMetadata.PACKAGE)),
				eq(EntityType.class))).thenAnswer(invocation -> Stream.of(entityType, entityType));

		when(dataService.getEntityTypeIds()).thenReturn(
				Stream.of("entity1", "entity2", "entity3", "entity4", "entity5"));

		date = Instant.parse("2016-01-01T12:34:28.123Z");

		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getId()).thenReturn("entityTypeName");

		reset(executorService);
	}

	@Test
	@WithMockUser
	public void testImportFile() throws IOException, URISyntaxException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.xlsx");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.xlsx"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = importRunFactory.create();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setOwner("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername().get(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, null, "add", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
	@WithMockUser
	public void testImportUpdateFile() throws IOException, URISyntaxException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.xlsx");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.xlsx"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = importRunFactory.create();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setOwner("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername().get(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, null, "update", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		assertEquals(response.getHeaders().getContentType(), TEXT_PLAIN);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
	@WithMockUser
	public void testImportIllegalUpdateModeFile() throws IOException, URISyntaxException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.xlsx");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.xlsx"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = importRunFactory.create();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setOwner("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername().get(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, null, "addsss", null);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(response.getHeaders().getContentType(), TEXT_PLAIN);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(0)).execute(captor.capture());
	}

	@Configuration
	@Import({ ImportTestConfig.class })
	static class Config
	{
		@Autowired
		private DataService dataService;

		@Bean
		public PasswordEncoder passwordEncoder()
		{
			return mock(PasswordEncoder.class);
		}

		@Bean
		public SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry()
		{
			return mock(SystemRepositoryDecoratorRegistry.class);
		}

		@Bean
		public UserService userService()
		{
			return mock(UserService.class);
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public MailSender mailSender()
		{
			return mock(MailSender.class);
		}

		@Bean
		public EntityTypeDependencyResolver entityTypeDependencyResolver()
		{
			return mock(EntityTypeDependencyResolver.class);
		}
	}
}
