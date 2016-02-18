package org.molgenis.data.importer;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.system.ImportRun;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes =
{ ImportApiTest.Config.class })
public class ImportApiTest extends AbstractTestNGSpringContextTests
{
	/**
	 * test exception for illigal name test exception for illegal databaseaction test happy emx test happy vcf test
	 * happy zip test exception for missing file
	 */
	ImportApi importApi;
	private ExecutorService executorService;
	private ImportRunService importRunService;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private FileStore fileStore;
	private ImportServiceFactory importServiceFactory;
	private FileRepositoryCollection repositoryCollection;
	private ImportService importService;
	private Date date;
	private DataService dataService;

	@BeforeClass
	public void beforeClass() throws ParseException
	{
		importServiceFactory = mock(ImportServiceFactory.class);
		fileStore = mock(FileStore.class);
		fileRepositoryCollectionFactory = mock(FileRepositoryCollectionFactory.class);
		importRunService = mock(ImportRunService.class);
		executorService = mock(ExecutorService.class);
		dataService = mock(DataService.class);
		importApi = new ImportApi(importServiceFactory, fileStore, fileRepositoryCollectionFactory, importRunService,
				dataService, executorService);
		repositoryCollection = mock(FileRepositoryCollection.class);
		importService = mock(ImportService.class);

		DateFormat format = new SimpleDateFormat("MM-DD-yyyy");
		date = format.parse("01-01-2016");
	}

	@BeforeMethod
	public void beforeTest() throws ParseException
	{
		reset(executorService);
	}

	@Test
	public void testImportFile() throws IOException
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
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "add", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
	public void testImportUpdateFile() throws IOException
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
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "update", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
	public void testImportIllagalUpdateModeFile() throws IOException
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
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "addsss", null);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(0)).execute(captor.capture());
	}

	@Test
	public void testImportVCFFile() throws IOException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.vcf");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.vcf"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "add", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
	public void testImportVCFFileNameSpecified() throws IOException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.vcf");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("newName.vcf"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, "newName", "add", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
	public void testImportUpdateVCF() throws IOException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.vcf");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.vcf"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "update", null);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(0)).execute(captor.capture());
	}

	@Test
	public void testImportUpdateVCFGZ() throws IOException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.vcf");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.vcf.gz"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "update", null);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(0)).execute(captor.capture());
	}

	@Test
	public void testImportAddVCFGZ() throws IOException
	{
		// set up the test
		HttpServletRequest request = mock(HttpServletRequest.class);
		File file = new File("/src/test/resources/example.vcf.gz");

		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
				file.getParentFile());
		fileItem.getOutputStream();
		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
		when(fileStore.store(streamCaptor.capture(), eq("example.vcf.gz"))).thenReturn(file);
		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
		ImportRun importRun = new ImportRun();
		importRun.setStartDate(date);
		importRun.setProgress(0);
		importRun.setStatus(ImportStatus.RUNNING.toString());
		importRun.setUserName("Harry");
		importRun.setNotify(false);
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = importApi.importFile(request, multipartFile, null, "add", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Configuration
	static class Config
	{
		@Bean
		public MailSender mailSender()
		{
			return mock(MailSender.class);
		}
	}
}
