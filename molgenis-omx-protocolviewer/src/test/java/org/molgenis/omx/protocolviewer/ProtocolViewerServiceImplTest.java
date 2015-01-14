package org.molgenis.omx.protocolviewer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Part;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.internal.InOrderImpl;
import org.molgenis.catalog.Catalog;
import org.molgenis.catalog.CatalogFolder;
import org.molgenis.catalog.CatalogMeta;
import org.molgenis.catalog.CatalogService;
import org.molgenis.catalog.UnknownCatalogException;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.excel.ExcelSheetWriter;
import org.molgenis.data.excel.ExcelWriter;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.protocolviewer.ProtocolViewerServiceImplTest.Config;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.study.StudyDefinition;
import org.molgenis.study.StudyDefinition.Status;
import org.molgenis.study.UnknownStudyDefinitionException;
import org.molgenis.studymanager.StudyManagerService;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes =
{ Config.class })
public class ProtocolViewerServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Configuration
	static class Config
	{
		@Bean
		public ProtocolViewerService orderStudyDataService()
		{
			return new ProtocolViewerServiceImpl();
		}

		@Bean
		public CatalogService catalogService()
		{
			return mock(CatalogService.class);
		}

		@Bean
		public JavaMailSender mailSender()
		{
			return mock(JavaMailSender.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

		@Bean
		public FileStore fileStore() throws IOException
		{
			return mock(FileStore.class);
		}

		@Bean
		public StudyManagerService studyManagerService()
		{
			return mock(StudyManagerService.class);
		}

		@Bean
		public MolgenisUserService molgenisUserService()
		{
			return mock(MolgenisUserService.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
	}

	private static Authentication AUTHENTICATION;

	@Autowired
	private ProtocolViewerService protocolViewerService;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private StudyManagerService studyManagerService;

	@Autowired
	private MolgenisUserService molgenisUserService;

	private final String username = "username";

	private Catalog catalog0, catalog1;

	private CatalogFolder catalogItem0, catalogItem1, catalogItem2;

	private CatalogMeta catalogMeta0, catalogMeta1, catalogMeta2;

	private StudyDefinition studyDefinitionCatalog0UserDraft, studyDefinitionCatalog1UserApproved;

	@BeforeClass
	public static void setUpBeforeClass()
	{
		AUTHENTICATION = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION);
	}

	@BeforeMethod
	public void setUp() throws IOException, UnknownCatalogException, UnknownStudyDefinitionException
	{
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(username);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		String molgenisUserEmail = "user@mail.com";
		MolgenisUser molgenisUser = mock(MolgenisUser.class);
		when(molgenisUser.getEmail()).thenReturn(molgenisUserEmail);
		when(molgenisUserService.getUser(username)).thenReturn(molgenisUser);

		File requestForm = mock(File.class);
		when(requestForm.getPath()).thenReturn("requestform.doc");
		when(fileStore.store(any(InputStream.class), any(String.class))).thenReturn(requestForm);

		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

		String catalog0Id = "0", catalog1Id = "1", catalog2Id = "2";
		String studyDefinition0Id = "0", studyDefinition1Id = "1";

		studyDefinitionCatalog0UserDraft = when(mock(StudyDefinition.class).getId()).thenReturn(studyDefinition0Id)
				.getMock();
		when(studyDefinitionCatalog0UserDraft.getStatus()).thenReturn(Status.DRAFT);

		studyDefinitionCatalog1UserApproved = when(mock(StudyDefinition.class).getId()).thenReturn(studyDefinition1Id)
				.getMock();
		when(studyDefinitionCatalog1UserApproved.getStatus()).thenReturn(Status.APPROVED);

		when(studyDefinitionCatalog0UserDraft.getAuthorEmail()).thenReturn(molgenisUserEmail);
		when(studyDefinitionCatalog1UserApproved.getAuthorEmail()).thenReturn(molgenisUserEmail);

		when(studyManagerService.getStudyDefinitions(username)).thenReturn(
				Arrays.asList(studyDefinitionCatalog0UserDraft, studyDefinitionCatalog1UserApproved));

		when(studyManagerService.createStudyDefinition(username, catalog0Id)).thenReturn(
				studyDefinitionCatalog0UserDraft);

		when(studyManagerService.getStudyDefinition(studyDefinition0Id)).thenReturn(studyDefinitionCatalog0UserDraft);
		when(studyManagerService.getStudyDefinition(studyDefinition1Id))
				.thenReturn(studyDefinitionCatalog1UserApproved);

		catalog0 = when(mock(Catalog.class).getId()).thenReturn(catalog0Id).getMock();
		catalog1 = when(mock(Catalog.class).getId()).thenReturn(catalog1Id).getMock();
		catalogMeta0 = new CatalogMeta(catalog0Id, "catalog #0");
		catalogMeta1 = new CatalogMeta(catalog1Id, "catalog #1");
		catalogMeta2 = new CatalogMeta(catalog2Id, "catalog #2");
		when(catalogService.getCatalogs()).thenReturn(Arrays.asList(catalogMeta0, catalogMeta1, catalogMeta2));
		when(catalogService.getCatalog(catalog0Id)).thenReturn(catalog0);
		when(catalogService.getCatalog(catalog1Id)).thenReturn(catalog1);
		when(catalogService.isCatalogLoaded(catalog0Id)).thenReturn(true);
		when(catalogService.isCatalogLoaded(catalog1Id)).thenReturn(true);
		when(catalogService.isCatalogLoaded(catalog2Id)).thenReturn(false);
		when(catalogService.getCatalogOfStudyDefinition(studyDefinition0Id)).thenReturn(catalog0);
		when(catalogService.getCatalogOfStudyDefinition(studyDefinition1Id)).thenReturn(catalog1);

		String catalogItem0Id = "0", catalogItem1Id = "1", catalogItem2Id = "2";
		catalogItem0 = when(mock(CatalogFolder.class).getExternalId()).thenReturn(catalogItem0Id).getMock();
		catalogItem1 = when(mock(CatalogFolder.class).getExternalId()).thenReturn(catalogItem1Id).getMock();
		catalogItem2 = when(mock(CatalogFolder.class).getExternalId()).thenReturn(catalogItem2Id).getMock();
		when(catalog0.findItem(catalogItem0Id)).thenReturn(catalogItem0);
		when(catalog0.findItem(catalogItem1Id)).thenReturn(catalogItem1);
		when(catalog0.findItem(catalogItem2Id)).thenReturn(catalogItem2);

		when(studyDefinitionCatalog0UserDraft.getItems()).thenReturn(
				Arrays.asList(catalogItem0, catalogItem1, catalogItem2));
	}

	@Test
	public void getCatalogs()
	{
		List<CatalogMeta> catalogs = Lists.newArrayList(protocolViewerService.getCatalogs().iterator());
		assertEquals(catalogs, Arrays.<CatalogMeta> asList(catalogMeta0, catalogMeta1));
	}

	@Test
	public void getStudyDefinitionDraftForCurrentUser() throws UnknownCatalogException
	{
		StudyDefinition studyDefinition = protocolViewerService.getStudyDefinitionDraftForCurrentUser(catalogMeta0
				.getId());
		assertEquals(studyDefinition, studyDefinitionCatalog0UserDraft);
	}

	@Test
	public void createStudyDefinitionDraftForCurrentUser() throws UnknownCatalogException
	{
		StudyDefinition studyDefinition = protocolViewerService.createStudyDefinitionDraftForCurrentUser(catalog0
				.getId());
		assertEquals(studyDefinition, studyDefinitionCatalog0UserDraft);
	}

	@Test(expectedExceptions = UnknownCatalogException.class)
	public void createStudyDefinitionDraftForCurrentUser_UnknownCatalogException() throws UnknownCatalogException
	{
		when(studyManagerService.createStudyDefinition(username, "unknown")).thenThrow(new UnknownCatalogException());
		protocolViewerService.createStudyDefinitionDraftForCurrentUser("unknown");
	}

	@Test
	public void getStudyDefinitionsForCurrentUser()
	{
		List<StudyDefinition> studyDefinitions = Lists.newArrayList(protocolViewerService
				.getStudyDefinitionsForCurrentUser());
		assertEquals(studyDefinitions,
				Arrays.asList(studyDefinitionCatalog0UserDraft, studyDefinitionCatalog1UserApproved));
	}

	@Test
	public void getStudyDefinitionForCurrentUser() throws NumberFormatException, UnknownStudyDefinitionException
	{
		StudyDefinition studyDefinition0 = protocolViewerService.getStudyDefinitionForCurrentUser(Integer
				.valueOf(studyDefinitionCatalog0UserDraft.getId()));
		assertEquals(studyDefinition0, studyDefinitionCatalog0UserDraft);
		StudyDefinition studyDefinition1 = protocolViewerService.getStudyDefinitionForCurrentUser(Integer
				.valueOf(studyDefinitionCatalog1UserApproved.getId()));
		assertEquals(studyDefinition1, studyDefinitionCatalog1UserApproved);
	}

	@Test
	public void submitStudyDefinitionDraftForCurrentUser() throws UnknownStudyDefinitionException,
			UnknownCatalogException, IOException, MessagingException
	{
		Part part = mock(Part.class);
		when(part.getInputStream()).thenReturn(new ByteArrayInputStream("bytes".getBytes("UTF-8")));
		protocolViewerService.submitStudyDefinitionDraftForCurrentUser("study", part, catalog0.getId().toString());
		verify(studyManagerService).updateStudyDefinition(any(StudyDefinition.class));
		verify(studyManagerService).submitStudyDefinition(studyDefinitionCatalog0UserDraft.getId(), catalog0.getId());
		verify(fileStore, times(2)).store(any(InputStream.class), any(String.class));
		verify(javaMailSender).send(any(MimeMessage.class));
	}

	@Test(expectedExceptions = UnknownCatalogException.class)
	public void addToStudyDefinitionDraftForCurrentUser_UnknownCatalogException() throws UnknownCatalogException
	{
		when(catalogService.getCatalog("unknown")).thenThrow(new UnknownCatalogException());
		protocolViewerService.addToStudyDefinitionDraftForCurrentUser("/api/v1/protocol/0", "unknown");
	}

	@Test(expectedExceptions = UnknownCatalogException.class)
	public void removeFromStudyDefinitionDraftForCurrentUser_UnknownCatalogException() throws UnknownCatalogException
	{
		when(catalogService.getCatalog("unknown")).thenThrow(new UnknownCatalogException());
		protocolViewerService.removeFromStudyDefinitionDraftForCurrentUser("/api/v1/protocol/0", "unknown");
	}

	@Test
	public void createStudyDefinitionDraftXlsForCurrentUser() throws IOException, UnknownCatalogException
	{
		ExcelWriter writer = mock(ExcelWriter.class);
		ExcelSheetWriter sheetWriter = mock(ExcelSheetWriter.class);
		when(writer.createWritable("Variables", Arrays.asList("Id", "Variable", "Description", "Group"))).thenReturn(sheetWriter);
		
		when(catalogItem0.getGroup()).thenReturn("Lifelines->Blah->blah");
		
		
		protocolViewerService.createStudyDefinitionDraftXlsForCurrentUser(writer, catalog0.getId());
		
		ArgumentCaptor<Entity> entityCaptor = ArgumentCaptor.forClass(Entity.class);
		verify(sheetWriter, times(3)).add(entityCaptor.capture());
		verify(sheetWriter).close();
		verify(writer).close();
		
		List<Entity> rows = entityCaptor.getAllValues();
		assertEquals("0", rows.get(0).get("Id"));
		assertEquals("1", rows.get(1).get("Id"));
		assertEquals("2", rows.get(2).get("Id"));
		
		assertEquals("Lifelines->Blah->blah", rows.get(0).get("Group"));
	}

	@Test
	public void createStudyDefinitionDraftXlsForCurrentUser_UnknownCatalogException() throws IOException,
			UnknownCatalogException
	{
		ExcelWriter writer = mock(ExcelWriter.class);
		protocolViewerService.createStudyDefinitionDraftXlsForCurrentUser(writer, "unknown");
		verifyNoMoreInteractions(writer);
	}
}
