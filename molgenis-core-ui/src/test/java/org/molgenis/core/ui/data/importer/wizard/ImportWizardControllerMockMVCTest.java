package org.molgenis.core.ui.data.importer.wizard;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.importer.ImportRunService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.molgenis.core.ui.data.importer.wizard.ImportWizardController.URI;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by mslofstra on 30/01/2018.
 */
public class ImportWizardControllerMockMVCTest extends AbstractMockitoTest
{
	@Mock
	private ImportWizardController controller;
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
	private GrantedAuthoritiesMapper grantedAuthoritiesMapper;
	@Mock
	private UserAccountService userAccountService;
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
	@Mock
	private org.molgenis.data.meta.MetaDataService metaDataService;
	@Mock
	private File file;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		controller = new ImportWizardController(uploadWizardPage, optionsWizardPage, packageWizardPage,
				validationResultWizardPage, importResultsWizardPage, dataService, importServiceFactory, fileStore,
				fileRepositoryCollectionFactory, importRunService, executorService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void testInvalidAction() throws Exception
	{
		MockMultipartFile vcfFile = new MockMultipartFile("file", "abcd".getBytes());
		when(fileStore.store(any(InputStream.class), eq(""))).thenReturn(file);
		mockMvc.perform(fileUpload(new URI(URI + "/importFile")).file(vcfFile).param("action", "bug"))
				.andExpect(status().isBadRequest()).andExpect(content()
				.string("Invalid action:[BUG] valid values: [ADD, ADD_UPDATE_EXISTING, UPDATE, ADD_IGNORE_EXISTING]"));
	}

	@Test
	public void testRepositoryExists() throws Exception
	{
		MockMultipartFile vcfFile = new MockMultipartFile("file", "file.vcf", null, "abcd".getBytes());
		when(importServiceFactory.getSupportedFileExtensions()).thenReturn(Collections.singleton("vcf"));
		when(fileStore.store(any(InputStream.class), eq("test.vcf"))).thenReturn(file);
		when(file.getName()).thenReturn("test.vcf");
		when(dataService.hasRepository("test")).thenReturn(true);
		mockMvc.perform(fileUpload(new URI(URI + "/importFile")).file(vcfFile).param("action", "add")
				.param("entityTypeId", "test")).andExpect(status().isBadRequest())
				.andExpect(content().string("A repository with name test already exists"));
	}
}