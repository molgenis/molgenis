package org.molgenis.core.ui.data.importer.wizard;

import com.google.common.collect.Lists;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.quality.Strictness;
import org.molgenis.core.ui.data.importer.wizard.ImportWizardControllerTest.Config;
import org.molgenis.data.*;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.support.FileRepositoryCollection;
import org.molgenis.data.importer.*;
import org.molgenis.data.importer.config.ImportTestConfig;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.config.GroupAuthorityTestConfig;
import org.molgenis.data.security.config.GroupTestConfig;
import org.molgenis.data.security.user.UserService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.Permission;
import org.molgenis.security.permission.PermissionManagerServiceImpl;
import org.molgenis.security.permission.Permissions;
import org.molgenis.security.user.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.security.core.Permission.COUNT;
import static org.molgenis.security.core.Permission.WRITE;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_PREFIX;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { Config.class })
public class ImportWizardControllerTest extends AbstractMolgenisSpringTest
{
	private ImportWizardController controller;
	private WebRequest webRequest;

	@Autowired
	private DataService dataService;

	@Autowired
	private GrantedAuthoritiesMapper grantedAuthoritiesMapper;

	@Autowired
	private UserAccountService userAccountService;

	@Autowired
	private ImportRunFactory importRunFactory;

	@Autowired
	private GroupFactory groupFactory;

	@Autowired
	private GroupAuthorityFactory groupAuthorityFactory;

	@Autowired
	private MetaDataService metaDataService;

	@Captor
	private ArgumentCaptor<GroupAuthority> groupAuthorityArgumentCaptor;

	private ImportServiceFactory importServiceFactory;
	private FileStore fileStore;
	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	private ImportRunService importRunService;
	private ExecutorService executorService;
	private FileRepositoryCollection repositoryCollection;
	private ImportService importService;
	private Instant date;
	private EntityType entityType;

	public ImportWizardControllerTest()
	{
		super(Strictness.WARN);
	}

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
				validationResultWizardPage, importResultsWizardPage, dataService, grantedAuthoritiesMapper,
				userAccountService, importServiceFactory, fileStore, fileRepositoryCollectionFactory, importRunService,
				executorService, groupAuthorityFactory);

		List<GroupAuthority> authorities = Lists.newArrayList();

		Group group1 = groupFactory.create();
		group1.setId("ID");
		group1.setActive(true);
		group1.setName("TestGroup");

		Entity entity1 = groupAuthorityFactory.create("entity1");
		entity1.set(AuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "entity1");
		entity1.set(GroupAuthorityMetaData.GROUP, group1);
		GroupAuthority authority1 = groupAuthorityFactory.create();
		authority1.set(entity1);

		Entity entity2 = groupAuthorityFactory.create("entity2");
		entity2.set(AuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "entity2");
		entity2.set(GroupAuthorityMetaData.GROUP, group1);
		GroupAuthority authority2 = groupAuthorityFactory.create();
		authority2.set(entity2);

		Entity entity3 = groupAuthorityFactory.create("entity3");
		entity3.set(AuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "entity3");
		entity3.set(GroupAuthorityMetaData.GROUP, group1);
		GroupAuthority authority3 = groupAuthorityFactory.create();
		authority3.set(entity3);

		Entity entity4 = groupAuthorityFactory.create("entity4");
		entity4.set(AuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "entity4");
		entity4.set(GroupAuthorityMetaData.GROUP, group1);
		GroupAuthority authority4 = groupAuthorityFactory.create();
		authority4.set(entity4);

		authorities.add(authority1);
		authorities.add(authority2);
		authorities.add(authority3);
		authorities.add(authority4);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity1,entity2");
		when(dataService.findOneById(GROUP, "ID", Group.class)).thenReturn(group1);
		when(dataService.findAll(GROUP_AUTHORITY,
				new QueryImpl<GroupAuthority>().eq(GroupAuthorityMetaData.GROUP, group1),
				GroupAuthority.class)).thenAnswer(
				invocation -> Stream.of(authority1, authority2, authority3, authority4));

		when(dataService.findAll(eq(EntityTypeMetadata.ENTITY_TYPE_META_DATA), any(),
				eq(new Fetch().field(EntityTypeMetadata.ID).field(EntityTypeMetadata.PACKAGE)),
				eq(EntityType.class))).thenAnswer(invocation -> Stream.of(entityType, entityType));

		when(dataService.findAll(GROUP_AUTHORITY,
				new QueryImpl<GroupAuthority>().eq(GroupAuthorityMetaData.GROUP, "ID"),
				GroupAuthority.class)).thenAnswer(
				invocation -> Stream.of(authority1, authority2, authority3, authority4));

		when(dataService.getEntityTypeIds()).thenReturn(
				Stream.of("entity1", "entity2", "entity3", "entity4", "entity5"));

		Authentication authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		GrantedAuthority grantedAuthority1 = new SimpleGrantedAuthority(authority1.getRole());
		GrantedAuthority grantedAuthority2 = new SimpleGrantedAuthority(authority2.getRole());
		GrantedAuthority grantedAuthority3 = new SimpleGrantedAuthority(authority3.getRole());
		GrantedAuthority grantedAuthority4 = new SimpleGrantedAuthority(authority4.getRole());
		UserDetails userDetails = mock(UserDetails.class);
		when(userDetails.getUsername()).thenReturn("username");
		when(userDetails.getPassword()).thenReturn("encoded-password");
		when((Collection<GrantedAuthority>) userDetails.getAuthorities()).thenReturn(
				asList(grantedAuthority1, grantedAuthority2, grantedAuthority3, grantedAuthority4));
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(
				asList(grantedAuthority1, grantedAuthority2, grantedAuthority3, grantedAuthority4));

		date = Instant.parse("2016-01-01T12:34:28.123Z");

		when(userAccountService.getCurrentUserGroups()).thenReturn(singletonList(group1));

		when(entityType.getId()).thenReturn("entityTypeId");
		when(entityType.getId()).thenReturn("entityTypeName");

		reset(executorService);
	}

	@Test
	public void getGroupEntityClassPermissionsTest()
	{
		Permissions permissions = controller.getGroupEntityClassPermissions("ID", webRequest);
		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();

		Permission permission = new Permission();
		permission.setType("writemeta");
		permission.setGroup("TestGroup");
		assertEquals(groupPermissions.get("entity1"), singletonList(permission));
		assertEquals(groupPermissions.get("entity2"), singletonList(permission));
		assertEquals(groupPermissions.get("entity3"), singletonList(permission));
		assertEquals(groupPermissions.get("entity4"), singletonList(permission));

		assertEquals(groupPermissions.size(), 4);
	}

	@Test
	public void addGroupEntityClassPermissionsTest()
	{
		User user = mock(User.class);
		when(user.isSuperuser()).thenReturn(false);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity4");
		when(webRequest.getParameter("radio-entity3")).thenReturn(COUNT.toString());
		when(webRequest.getParameter("radio-entity4")).thenReturn(WRITE.toString());

		GroupAuthority authority = groupAuthorityFactory.create();
		authority.setGroup(dataService.findOneById(GROUP, "ID", Group.class));
		authority.setRole(AUTHORITY_ENTITY_PREFIX + COUNT.toString().toUpperCase() + '_' + "entity3");

		controller.addGroupEntityClassPermissions("ID", webRequest);

		verify(dataService, times(2)).update(eq(GROUP_AUTHORITY), any(GroupAuthority.class));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void addGroupEntityClassPermissionsTestNoPermission()
	{
		User user = mock(User.class);
		when(user.isSuperuser()).thenReturn(false);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity5");
		when(webRequest.getParameter("radio-entity3")).thenReturn(COUNT.toString());
		when(webRequest.getParameter("radio-entity5")).thenReturn(WRITE.toString());
		controller.addGroupEntityClassPermissions("ID", webRequest);
	}

	@Test()
	public void addGroupEntityClassPermissionsTestNoPermissionSU()
	{
		User user = mock(User.class);
		when(user.isSuperuser()).thenReturn(true);
		when(userAccountService.getCurrentUser()).thenReturn(user);

		webRequest = mock(WebRequest.class);
		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity5");
		when(webRequest.getParameter("radio-entity3")).thenReturn(COUNT.toString());
		when(webRequest.getParameter("radio-entity5")).thenReturn(WRITE.toString());

		controller.addGroupEntityClassPermissions("ID", webRequest);

		verify(dataService).update(eq(GROUP_AUTHORITY), groupAuthorityArgumentCaptor.capture());
		assertEquals(groupAuthorityArgumentCaptor.getValue().getRole(), "ROLE_ENTITY_COUNT_entity3");
		assertEquals(groupAuthorityArgumentCaptor.getValue().getGroup(),
				dataService.findOneById(GROUP, "ID", Group.class));

		verify(dataService).add(eq(GROUP_AUTHORITY), groupAuthorityArgumentCaptor.capture());
		assertEquals(groupAuthorityArgumentCaptor.getValue().getRole(), "ROLE_ENTITY_WRITE_entity5");
		assertEquals(groupAuthorityArgumentCaptor.getValue().getGroup(),
				dataService.findOneById(GROUP, "ID", Group.class));
	}

	@Test
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
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, null, "add", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
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
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, null, "update", null);
		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
		assertEquals(response.getHeaders().getContentType(), TEXT_PLAIN);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(1)).execute(captor.capture());
	}

	@Test
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
		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);

		// the actual test
		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, null, "addsss", null);
		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
		assertEquals(response.getHeaders().getContentType(), TEXT_PLAIN);

		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
		verify(executorService, times(0)).execute(captor.capture());
	}

	@Configuration
	@Import({ ImportTestConfig.class, GroupTestConfig.class, GroupAuthorityTestConfig.class })
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
		public PermissionManagerServiceImpl pluginPermissionManagerServiceImpl()
		{
			return new PermissionManagerServiceImpl(dataService, grantedAuthoritiesMapper());
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public GrantedAuthoritiesMapper grantedAuthoritiesMapper()
		{
			return mock(GrantedAuthoritiesMapper.class);
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return mock(UserAccountService.class);
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
