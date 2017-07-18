package org.molgenis.standardsregistry;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.standardsregistry.model.*;
import org.molgenis.standardsregistry.services.MetaDataSearchService;
import org.molgenis.standardsregistry.services.TreeNodeService;
import org.molgenis.standardsregistry.utils.StandardRegistryTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.ui.ExtendedModelMap;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * <p>Test for <code>StandardRegistryTest</code>.</p>
 *
 * @author sido
 */
@ContextConfiguration(classes = { TestHarnessConfig.class,
		StandardsRegistryControllerTest.StandardRegistryControllerTestConfig.class })
public class StandardsRegistryControllerTest extends AbstractMolgenisSpringTest
{

	@Autowired
	private StandardRegistryTestHarness standardRegistryTestHarness;

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private MetaDataSearchService metaDataSearchService;

	@Autowired
	private TreeNodeService treeNodeService;

	@Autowired
	private StandardsRegistryController standardRegistryController;

	@Test
	public void testInit() {
		ExtendedModelMap modelWarning = new ExtendedModelMap();
		String templateWarning = standardRegistryController.init("true", modelWarning);
		assertEquals(templateWarning, "view-model-standardsregistry");
		assertEquals(modelWarning.asMap().get("warningMessage"), "Package not found");

		ExtendedModelMap modelNoWarning = new ExtendedModelMap();
		String templateNoWarning = standardRegistryController.init("false", modelNoWarning);
		assertEquals(templateNoWarning, "view-model-standardsregistry");
		assertEquals(modelNoWarning.asMap().get("warningMessage"), null);

	}

	@Test
	public void testDocumentation() throws Exception
	{
		String TEST_PACKAGE = "test-package";

		Iterable<Package> packages = Lists.newArrayList(packageFactory.create(TEST_PACKAGE));
		when(metaDataService.getRootPackages()).thenReturn(packages);

		ExtendedModelMap modelDocs = new ExtendedModelMap();
		String templateDocs = standardRegistryController.getModelDocumentation(modelDocs);
		assertEquals(templateDocs, "view-model-standardsregistry_docs");
		List<Package> packagesResponse = (ArrayList) modelDocs.asMap().get("packages");
		assertEquals(packagesResponse, packages);

		String TEST_SINGLE_PACKAGE = "test-single-package";
		Package pkg = packageFactory.create(TEST_SINGLE_PACKAGE);
		when(metaDataService.getPackage(TEST_SINGLE_PACKAGE)).thenReturn(pkg);

		ExtendedModelMap modelDocsMacros = new ExtendedModelMap();
		String templateDocsMacros = standardRegistryController.getModelDocumentation(TEST_SINGLE_PACKAGE, true, modelDocsMacros);
		assertEquals(templateDocsMacros,"view-model-standardsregistry_docs-macros");
		Package pkgResponse = (Package) modelDocsMacros.asMap().get("package");
		assertEquals(pkgResponse, pkg);

	}

	@Test
	public void testSearch() throws Exception
	{
		String TEST_PACKAGE = "test-package";
		Gson gson = new Gson();

		PackageResponse packageResponse = new PackageResponse("test-package-response", "", "", null, null, null);
		PackageSearchResponse packageSearchResponse = new PackageSearchResponse(TEST_PACKAGE, 0, 3, 0, Lists.newArrayList(packageResponse));
//		when(metaDataSearchService.search()).thenReturn(packageSearchResponse);

		ExtendedModelMap model = new ExtendedModelMap();
		String template = standardRegistryController.search(TEST_PACKAGE, model);
		assertEquals(template, "view-model-standardsregistry");
		assertEquals(model.asMap().get("packageSearchResponse"), gson.toJson(packageSearchResponse));

	}

	@Test
	public void testSearchPackageNotFound() throws Exception
	{
		String TEST_PACKAGE = "test-package";
//		when(metaDataSearchService.search(TEST_PACKAGE, 3, 0)).thenReturn(null);

		ExtendedModelMap model = new ExtendedModelMap();
		standardRegistryController.search(TEST_PACKAGE, model);
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
		String template = standardRegistryController.showView(TEST_PACKAGE, model);
		assertEquals(template, "view-model-standardsregistry_details");
		assertEquals(model.get("selectedPackageName"), TEST_PACKAGE);
		assertEquals(model.get("package"), selectedPackage);
	}

	@Test
	public void testGetUml() throws Exception
	{
		String TEST_PACKAGE = "test-package";
		Package pkg = packageFactory.create(TEST_PACKAGE);
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(pkg);

		ExtendedModelMap model = new ExtendedModelMap();
		String template = standardRegistryController.getUml(TEST_PACKAGE, model);
		assertEquals(template, "view-model-standardsregistry_uml");
		assertEquals(model.get("molgenisPackage"), pkg);

	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testUmlPackageNotFound() {
		String TEST_PACKAGE = "test-package";
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(null);
		ExtendedModelMap model = new ExtendedModelMap();
		standardRegistryController.getUml(TEST_PACKAGE, model);
	}

	@Test
	public void testGetPackage() throws Exception
	{
		String TEST_PACKAGE = "test-package";
		Package molgenisPackge = packageFactory.create(TEST_PACKAGE);
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(molgenisPackge);

		StandardRegistryEntity entity = standardRegistryTestHarness.createStandardRegistryEntity();
		List<StandardRegistryEntity> entities = Lists.newArrayList(entity);
		when(metaDataSearchService.getEntitiesInPackage(TEST_PACKAGE)).thenReturn(entities);

		StandardRegistryTag tag = standardRegistryTestHarness.createStandardRegistryTag();
		List<StandardRegistryTag> tags = Lists.newArrayList(tag);
		when(metaDataSearchService.getTagsForPackage(molgenisPackge)).thenReturn(tags);

		PackageResponse pacakgeResponse = new PackageResponse(TEST_PACKAGE, "", "", null, entities, tags);
		PackageResponse response = standardRegistryController.getPackage(TEST_PACKAGE);
		assertEquals(response.getEntities().get(0).getName(), pacakgeResponse.getEntities().get(0).getName());
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testPackageNotFound() {
		String TEST_PACKAGE = "test-package";
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(null);
		standardRegistryController.getPackage(TEST_PACKAGE);
	}

	@Test
	public void testGetTreeData() throws Exception
	{

		String TEST_PACKAGE = "test-package";
		Package molgenisPackage = packageFactory.create(TEST_PACKAGE);
		PackageTreeNode treeNode = standardRegistryTestHarness.createPackageTreeNode();
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(molgenisPackage);
		when(treeNodeService.createPackageTreeNode(molgenisPackage)).thenReturn(treeNode);

		Collection<PackageTreeNode> response = standardRegistryController.getTree(TEST_PACKAGE);
		assertEquals(response, Collections.singletonList(treeNode));
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void testGetTreeDataNotFound()
	{
		String TEST_PACKAGE = "test-package";
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(null);
		standardRegistryController.getTree(TEST_PACKAGE);
	}

	@Configuration
	@Import(StandardRegistryTestHarness.class)
	public static class StandardRegistryControllerTestConfig
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
		public StandardsRegistryController standardRegistryController()
		{
			return new StandardsRegistryController(metaDataService(), metaDataSearchService(), tagService(),
					treeNodeService());
		}

	}

}
