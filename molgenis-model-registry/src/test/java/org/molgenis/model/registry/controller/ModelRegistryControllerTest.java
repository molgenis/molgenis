package org.molgenis.model.registry.controller;

import com.google.common.collect.Lists;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.model.registry.model.*;
import org.molgenis.model.registry.services.MetaDataSearchService;
import org.molgenis.model.registry.services.TreeNodeService;
import org.molgenis.model.registry.utils.ModelRegistryTestHarness;
import org.molgenis.ui.menu.Menu;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { TestHarnessConfig.class,
		ModelRegistryControllerTest.ModelRegistryControllerTestConfig.class })
public class ModelRegistryControllerTest extends AbstractMolgenisSpringTest
{

	@Autowired
	private ModelRegistryTestHarness modelRegistryTestHarness;

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private MetaDataSearchService metaDataSearchService;

	@Autowired
	private MenuReaderService menuReaderService;

	@Autowired
	private TreeNodeService treeNodeService;

	@Autowired
	private ModelRegistryController modelRegistryController;

	@Test
	public void testInit()
	{
		ExtendedModelMap modelWarning = new ExtendedModelMap();
		String templateWarning = modelRegistryController.init("true", modelWarning);
		assertEquals(templateWarning, "view-model-registry");
		assertEquals(modelWarning.asMap().get("warningMessage"), "Package not found");

		ExtendedModelMap modelNoWarning = new ExtendedModelMap();
		String templateNoWarning = modelRegistryController.init("false", modelNoWarning);
		assertEquals(templateNoWarning, "view-model-registry");
		assertEquals(modelNoWarning.asMap().get("warningMessage"), null);

	}

	@Test
	public void testDocumentation() throws Exception
	{
		String TEST_PACKAGE = "test-package";

		Iterable<Package> packages = Lists.newArrayList(packageFactory.create(TEST_PACKAGE));
		when(metaDataService.getRootPackages()).thenReturn(packages);

		ExtendedModelMap modelDocs = new ExtendedModelMap();
		String templateDocs = modelRegistryController.getModelDocumentation(modelDocs);
		assertEquals(templateDocs, "view-model-registry_docs");
		List<Package> packagesResponse = (List<Package>) modelDocs.asMap().get("packages");
		assertEquals(packagesResponse, packages);

		String TEST_SINGLE_PACKAGE = "test-single-package";
		Package pkg = packageFactory.create(TEST_SINGLE_PACKAGE);
		when(metaDataService.getPackage(TEST_SINGLE_PACKAGE)).thenReturn(pkg);

		ExtendedModelMap modelDocsMacros = new ExtendedModelMap();
		String templateDocsMacros = modelRegistryController.getModelDocumentation(TEST_SINGLE_PACKAGE, true,
				modelDocsMacros);
		assertEquals(templateDocsMacros, "view-model-registry_docs-macros");
		Package pkgResponse = (Package) modelDocsMacros.asMap().get("package");
		assertEquals(pkgResponse, pkg);

	}

	@Test
	public void testSearch() throws Exception
	{
		String TEST_PACKAGE = "test-package";

		ModelRegistryPackage modelRegistryPackage = ModelRegistryPackage.create("test-package-response", "", "", null,
				null, null);
		ModelRegistrySearch packageSearchResponse = ModelRegistrySearch.create(TEST_PACKAGE, 0, 3, 0,
				Lists.newArrayList(modelRegistryPackage));
		when(metaDataSearchService.search(TEST_PACKAGE, 0, 3)).thenReturn(packageSearchResponse);

		ExtendedModelMap model = new ExtendedModelMap();
		String template = modelRegistryController.search(TEST_PACKAGE, model);
		assertEquals(template, "view-model-registry");
		assertEquals(model.asMap().get("packageSearchResponse"), packageSearchResponse);

	}

	@Test
	public void testSearchPackageNotFound() throws Exception
	{
		String TEST_PACKAGE = "test-package";
		when(metaDataSearchService.search(TEST_PACKAGE, 0, 3)).thenReturn(null);

		ExtendedModelMap model = new ExtendedModelMap();
		modelRegistryController.search(TEST_PACKAGE, model);
		assertEquals(model.asMap().get("packageSearchResponse"), null);
	}

	@Test
	public void testShowView() throws Exception
	{
		String TEST_PACKAGE = "test-package";
		Package selectedPackage = packageFactory.create(TEST_PACKAGE);
		Iterable<Package> packages = Lists.newArrayList(selectedPackage);
		when(metaDataService.getRootPackages()).thenReturn(packages);
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(selectedPackage);

		ExtendedModelMap model = new ExtendedModelMap();
		String template = modelRegistryController.showView(TEST_PACKAGE, model);
		assertEquals(template, "view-model-registry_details");
		assertEquals(model.get("selectedPackageName"), TEST_PACKAGE);
		assertEquals(model.get("package"), selectedPackage);
	}

	@Test
	public void testGetUml() throws Exception
	{
		String TEST_PACKAGE = "test-package";

		Menu menu = mock(Menu.class);
		when(menu.findMenuItemPath(ModelRegistryController.ID)).thenReturn("/test/path");
		when(menuReaderService.getMenu()).thenReturn(menu);

		ExtendedModelMap model = new ExtendedModelMap();
		String template = modelRegistryController.getUml(TEST_PACKAGE, model);
		assertEquals(template, "view-model-registry_uml");
		assertEquals(model.get("molgenisPackage"), TEST_PACKAGE);

	}

	@Test
	public void testGetPackage() throws Exception
	{
		String TEST_PACKAGE = "test-package";
		Package molgenisPackge = packageFactory.create(TEST_PACKAGE);
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(molgenisPackge);

		ModelRegistryEntity entity = modelRegistryTestHarness.createStandardRegistryEntity();
		List<ModelRegistryEntity> entities = Lists.newArrayList(entity);
		when(metaDataSearchService.getEntitiesInPackage(TEST_PACKAGE)).thenReturn(entities);

		ModelRegistryTag tag = modelRegistryTestHarness.createStandardRegistryTag();
		List<ModelRegistryTag> tags = Lists.newArrayList(tag);
		when(metaDataSearchService.getTagsForPackage(molgenisPackge)).thenReturn(tags);

		ModelRegistryPackage pacakgeResponse = ModelRegistryPackage.create(TEST_PACKAGE, "", "", null, entities, tags);
		ModelRegistryPackage response = modelRegistryController.getPackage(TEST_PACKAGE);
		assertEquals(response.getEntities().get(0).getName(), pacakgeResponse.getEntities().get(0).getName());
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testPackageNotFound()
	{
		String TEST_PACKAGE = "test-package";
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(null);
		modelRegistryController.getPackage(TEST_PACKAGE);
	}

	@Test
	public void testGetTreeData() throws Exception
	{

		String TEST_PACKAGE = "test-package";
		Package molgenisPackage = packageFactory.create(TEST_PACKAGE);
		ModelRegistryTreeNode treeNode = modelRegistryTestHarness.createPackageTreeNode();
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(molgenisPackage);
		when(treeNodeService.createTreeNode(molgenisPackage)).thenReturn(treeNode);

		Collection<ModelRegistryTreeNode> response = modelRegistryController.getTree(TEST_PACKAGE);
		assertEquals(response, Collections.singletonList(treeNode));
	}

	@Configuration
	@Import(ModelRegistryTestHarness.class)
	public static class ModelRegistryControllerTestConfig
	{
		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		public MetaDataSearchService metaDataSearchService()
		{
			return mock(MetaDataSearchService.class);
		}

		@Bean
		public TagService<LabeledResource, LabeledResource> tagService()
		{
			return mock(TagService.class);
		}

		@Bean
		public TreeNodeService treeNodeService()
		{
			return mock(TreeNodeService.class);
		}

		@Bean
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

		@Bean
		public LanguageService languageService()
		{
			return mock(LanguageService.class);
		}

		@Bean
		public AppSettings appSettings()
		{
			return mock(AppSettings.class);
		}

		@Bean
		public MenuReaderService menuReaderService()
		{
			return mock(MenuReaderService.class);
		}

		@Bean
		public ModelRegistryController standardRegistryController()
		{
			return new ModelRegistryController(metaDataService(), metaDataSearchService(), tagService(),
					treeNodeService(), languageService(), appSettings(), menuReaderService());
		}

	}

}
