package org.molgenis.data.importer;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//import org.molgenis.data.importer.ImportWizardControllerTest.Config;

///**
// * Created by charbonb on 17/03/15.
// */
//@ContextConfiguration(classes =
//{ Config.class })
public class ImportWizardControllerTest extends AbstractTestNGSpringContextTests
{
	//	private ImportWizardController controller;
	//	private WebRequest webRequest;
	//
	//	@Autowired
	//	DataService dataService;
	//
	//	@Autowired
	//	GrantedAuthoritiesMapper grantedAuthoritiesMapper;
	//
	//	@Autowired
	//	UserAccountService userAccountService;
	//	private Authentication authentication;
	//	private UserDetails userDetails;
	//	private ImportServiceFactory importServiceFactory;
	//	private FileStore fileStore;
	//	private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
	//	private ImportRunService importRunService;
	//	private ExecutorService executorService;
	//	private FileRepositoryCollection repositoryCollection;
	//	private ImportService importService;
	//	private Date date;
	//
	//	@Configuration
	//	static class Config
	//	{
	//		@Bean
	//		public PermissionManagerServiceImpl pluginPermissionManagerServiceImpl()
	//		{
	//			return new PermissionManagerServiceImpl(dataService(), molgenisPluginRegistry(),
	//					grantedAuthoritiesMapper());
	//		}
	//
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public MolgenisPluginRegistry molgenisPluginRegistry()
	//		{
	//			return mock(MolgenisPluginRegistry.class);
	//		}
	//
	//		@Bean
	//		public GrantedAuthoritiesMapper grantedAuthoritiesMapper()
	//		{
	//			return mock(GrantedAuthoritiesMapper.class);
	//		}
	//
	//		@Bean
	//		public UserAccountService userAccountService()
	//		{
	//
	//			UserAccountService userAccountService = mock(UserAccountService.class);
	//			MolgenisGroup group1 = new MolgenisGroup();
	//			group1.setId("ID");
	//			group1.setActive(true);
	//			group1.setName("TestGroup");
	//			when(userAccountService.getCurrentUserGroups()).thenReturn(Collections.singletonList(group1));
	//			return userAccountService;
	//		}
	//
	//		@Bean
	//		public MailSender mailSender()
	//		{
	//			return mock(MailSender.class);
	//		}
	//	}
	//
	//	@BeforeMethod
	//	public void setUp() throws ParseException {
	//		reset(dataService);
	//		UploadWizardPage uploadWizardPage = mock(UploadWizardPage.class);
	//		OptionsWizardPage optionsWizardPage = mock(OptionsWizardPage.class);
	//		ValidationResultWizardPage validationResultWizardPage = mock(ValidationResultWizardPage.class);
	//		ImportResultsWizardPage importResultsWizardPage = mock(ImportResultsWizardPage.class);
	//		PackageWizardPage packageWizardPage = mock(PackageWizardPage.class);
	//		importServiceFactory = mock(ImportServiceFactory.class);
	//		fileStore = mock(FileStore.class);
	//		fileRepositoryCollectionFactory = mock(FileRepositoryCollectionFactory.class);
	//		importRunService = mock(ImportRunService.class);
	//		executorService = mock(ExecutorService.class);
	//		dataService = mock(DataService.class);
	//		repositoryCollection = mock(FileRepositoryCollection.class);
	//		importService = mock(ImportService.class);
	//
	//		controller = new ImportWizardController(uploadWizardPage, optionsWizardPage, packageWizardPage,
	//				validationResultWizardPage, importResultsWizardPage, dataService, grantedAuthoritiesMapper,
	//				userAccountService, importServiceFactory, fileStore, fileRepositoryCollectionFactory, importRunService,
	//				executorService, );
	//
	//		List<GroupAuthority> authorities = new ArrayList<>();
	//
	//		MolgenisGroup group1 = new MolgenisGroup();
	//		group1.setId("ID");
	//		group1.setActive(true);
	//		group1.setName("TestGroup");
	//
	//		MapEntity entity1 = new MapEntity("Entity1");
	//		entity1.set(GroupAuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY1");
	//		entity1.set(GroupAuthorityMetaData.MOLGENISGROUP, group1);
	//		GroupAuthority authority1 = new GroupAuthority();
	//		authority1.set(entity1);
	//
	//		MapEntity entity2 = new MapEntity("Entity2");
	//		entity2.set(GroupAuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY2");
	//		entity2.set(GroupAuthorityMetaData.MOLGENISGROUP, group1);
	//		GroupAuthority authority2 = new GroupAuthority();
	//		authority2.set(entity2);
	//
	//		MapEntity entity3 = new MapEntity("Entity2");
	//		entity3.set(GroupAuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY3");
	//		entity3.set(GroupAuthorityMetaData.MOLGENISGROUP, group1);
	//		GroupAuthority authority3 = new GroupAuthority();
	//		authority3.set(entity3);
	//
	//		MapEntity entity4 = new MapEntity("Entity2");
	//		entity4.set(GroupAuthorityMetaData.ROLE, SecurityUtils.AUTHORITY_ENTITY_WRITEMETA_PREFIX + "ENTITY4");
	//		entity4.set(GroupAuthorityMetaData.MOLGENISGROUP, group1);
	//		GroupAuthority authority4 = new GroupAuthority();
	//		authority4.set(entity4);
	//
	//		authorities.add(authority1);
	//		authorities.add(authority2);
	//		authorities.add(authority3);
	//		authorities.add(authority4);
	//
	//		webRequest = mock(WebRequest.class);
	//		when(webRequest.getParameter("entityIds")).thenReturn("entity1,entity2");
	//		when(dataService.findOneById(MolgenisGroupMetaData.TAG, "ID", MolgenisGroup.class)).thenReturn(group1);
	//		when(dataService.findAll(GroupAuthorityMetaData.TAG, new QueryImpl<GroupAuthority>().eq(
	//				GroupAuthorityMetaData.MOLGENISGROUP, group1),
	//				GroupAuthority.class)).thenAnswer(new Answer<Stream<GroupAuthority>>()
	//				{
	//					@Override
	//					public Stream<GroupAuthority> answer(InvocationOnMock invocation) throws Throwable
	//					{
	//						return Stream.of(authority1, authority2, authority3, authority4);
	//					}
	//				});
	//		when(dataService.findAll(GroupAuthorityMetaData.TAG, new QueryImpl<GroupAuthority>().eq(
	//				GroupAuthorityMetaData.MOLGENISGROUP, "ID"),
	//				GroupAuthority.class)).thenAnswer(new Answer<Stream<GroupAuthority>>()
	//				{
	//					@Override
	//					public Stream<GroupAuthority> answer(InvocationOnMock invocation) throws Throwable
	//					{
	//						return Stream.of(authority1, authority2, authority3, authority4);
	//					}
	//				});
	//		when(dataService.getEntityNames()).thenReturn(Stream.of("entity1", "entity2", "entity3", "entity4", "entity5"));
	//
	//		authentication = mock(Authentication.class);
	//		SecurityContextHolder.getContext().setAuthentication(authentication);
	//		GrantedAuthority grantedAuthority1 = new SimpleGrantedAuthority(authority1.getRole().toString());
	//		GrantedAuthority grantedAuthority2 = new SimpleGrantedAuthority(authority2.getRole().toString());
	//		GrantedAuthority grantedAuthority3 = new SimpleGrantedAuthority(authority3.getRole().toString());
	//		GrantedAuthority grantedAuthority4 = new SimpleGrantedAuthority(authority4.getRole().toString());
	//		userDetails = mock(UserDetails.class);
	//		when(userDetails.getUsername()).thenReturn("username");
	//		when(userDetails.getPassword()).thenReturn("encoded-password");
	//		when((Collection<GrantedAuthority>) userDetails.getAuthorities()).thenReturn(Arrays
	//				.<GrantedAuthority> asList(grantedAuthority1, grantedAuthority2, grantedAuthority3, grantedAuthority4));
	//		when(authentication.getPrincipal()).thenReturn(userDetails);
	//		when((Collection<GrantedAuthority>) authentication.getAuthorities()).thenReturn(Arrays
	//				.<GrantedAuthority> asList(grantedAuthority1, grantedAuthority2, grantedAuthority3, grantedAuthority4));
	//
	//		DateFormat format = new SimpleDateFormat("MM-DD-yyyy");
	//		date = format.parse("01-01-2016");
	//
	//		reset(executorService);
	//	}
	//
	//	@Test
	//	public void getGroupEntityClassPermissionsTest()
	//	{
	//		Permissions permissions = controller.getGroupEntityClassPermissions("ID", webRequest);
	//		Map<String, List<Permission>> groupPermissions = permissions.getGroupPermissions();
	//
	//		Permission permission = new Permission();
	//		permission.setType("writemeta");
	//		permission.setGroup("TestGroup");
	//		assertEquals(groupPermissions.get("entity1"), Arrays.asList(permission));
	//		assertEquals(groupPermissions.get("entity2"), Arrays.asList(permission));
	//		assertEquals(groupPermissions.get("entity3"), Arrays.asList(permission));
	//		assertEquals(groupPermissions.get("entity4"), Arrays.asList(permission));
	//
	//		assertEquals(groupPermissions.size(), 4);
	//	}
	//
	//	@Test
	//	public void addGroupEntityClassPermissionsTest()
	//	{
	//		MolgenisUser user = mock(MolgenisUser.class);
	//		when(user.isSuperuser()).thenReturn(false);
	//		when(userAccountService.getCurrentUser()).thenReturn(user);
	//
	//		webRequest = mock(WebRequest.class);
	//		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity4");
	//		when(webRequest.getParameter("radio-entity3"))
	//				.thenReturn(org.molgenis.security.core.Permission.COUNT.toString());
	//		when(webRequest.getParameter("radio-entity4"))
	//				.thenReturn(org.molgenis.security.core.Permission.WRITE.toString());
	//
	//		GroupAuthority authority = new GroupAuthority();
	//		authority.setMolgenisGroup(dataService.findOneById(MolgenisGroupMetaData.TAG, "ID", MolgenisGroup.class));
	//		authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX
	//				+ org.molgenis.security.core.Permission.COUNT.toString().toUpperCase() + "_" + "entity3".toUpperCase());
	//
	//		controller.addGroupEntityClassPermissions("ID", webRequest);
	//
	//		verify(dataService, times(2)).add(GroupAuthorityMetaData.TAG, authority);
	//	}
	//
	//	@Test(expectedExceptions = MolgenisDataAccessException.class)
	//	public void addGroupEntityClassPermissionsTestNoPermission()
	//	{
	//		MolgenisUser user = mock(MolgenisUser.class);
	//		when(user.isSuperuser()).thenReturn(false);
	//		when(userAccountService.getCurrentUser()).thenReturn(user);
	//
	//		webRequest = mock(WebRequest.class);
	//		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity5");
	//		when(webRequest.getParameter("radio-entity3"))
	//				.thenReturn(org.molgenis.security.core.Permission.COUNT.toString());
	//		when(webRequest.getParameter("radio-entity5"))
	//				.thenReturn(org.molgenis.security.core.Permission.WRITE.toString());
	//		controller.addGroupEntityClassPermissions("ID", webRequest);
	//
	//	}
	//
	//	@Test()
	//	public void addGroupEntityClassPermissionsTestNoPermissionSU()
	//	{
	//		MolgenisUser user = mock(MolgenisUser.class);
	//		when(user.isSuperuser()).thenReturn(true);
	//		when(userAccountService.getCurrentUser()).thenReturn(user);
	//
	//		webRequest = mock(WebRequest.class);
	//		when(webRequest.getParameter("entityIds")).thenReturn("entity3,entity5");
	//		when(webRequest.getParameter("radio-entity3"))
	//				.thenReturn(org.molgenis.security.core.Permission.COUNT.toString());
	//		when(webRequest.getParameter("radio-entity5"))
	//				.thenReturn(org.molgenis.security.core.Permission.WRITE.toString());
	//
	//		GroupAuthority authority = new GroupAuthority();
	//		authority.setMolgenisGroup(dataService.findOneById(MolgenisGroupMetaData.TAG, "ID", MolgenisGroup.class));
	//		authority.setRole(SecurityUtils.AUTHORITY_ENTITY_PREFIX
	//				+ org.molgenis.security.core.Permission.COUNT.toString().toUpperCase() + "_" + "entity3".toUpperCase());
	//
	//		controller.addGroupEntityClassPermissions("ID", webRequest);
	//
	//		verify(dataService, times(2)).add(GroupAuthorityMetaData.TAG, authority);
	//
	//	}
	//
	//
	//	@Test
	//	public void testImportFile() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.xlsx");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.xlsx"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "add", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(1)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportUpdateFile() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.xlsx");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.xlsx"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "update", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(1)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportIllegalUpdateModeFile() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.xlsx");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.xlsx"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "addsss", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(0)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportVCFFile() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.vcf");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.vcf"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "add", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(1)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportVCFFileNameSpecified() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.vcf");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("newName.vcf"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, "newName", "add", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(1)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportUpdateVCF() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.vcf");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.vcf"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "update", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(0)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportUpdateVCFGZ() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.vcf");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.vcf.gz"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "update", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(0)).execute(captor.capture());
	//	}
	//
	//	@Test
	//	public void testImportAddVCFGZ() throws IOException, URISyntaxException
	//	{
	//		// set up the test
	//		HttpServletRequest request = mock(HttpServletRequest.class);
	//		File file = new File("/src/test/resources/example.vcf.gz");
	//
	//		DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length(),
	//				file.getParentFile());
	//		fileItem.getOutputStream();
	//		MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	//		ArgumentCaptor<InputStream> streamCaptor = ArgumentCaptor.forClass(InputStream.class);
	//		when(fileStore.store(streamCaptor.capture(), eq("example.vcf.gz"))).thenReturn(file);
	//		when(fileRepositoryCollectionFactory.createFileRepositoryCollection(file)).thenReturn(repositoryCollection);
	//		when(importServiceFactory.getImportService(file.getName())).thenReturn(importService);
	//		ImportRun importRun = new ImportRun();
	//		importRun.setStartDate(date);
	//		importRun.setProgress(0);
	//		importRun.setStatus(ImportStatus.RUNNING.toString());
	//		importRun.setUserName("Harry");
	//		importRun.setNotify(false);
	//		when(importRunService.addImportRun(SecurityUtils.getCurrentUsername(), false)).thenReturn(importRun);
	//
	//		// the actual test
	//		ResponseEntity<String> response = controller.importFile(request, multipartFile, null, "add", null);
	//		assertEquals(response.getStatusCode(), HttpStatus.CREATED);
	//		assertEquals(response.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
	//
	//		ArgumentCaptor<ImportJob> captor = ArgumentCaptor.forClass(ImportJob.class);
	//		verify(executorService, times(1)).execute(captor.capture());
	//	}
}
