package org.molgenis.model.registry.services;

import com.google.common.collect.Lists;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityTestHarness;
import org.molgenis.data.Query;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.model.registry.model.ModelRegistrySearch;
import org.molgenis.model.registry.model.PackageSearchRequest;
import org.molgenis.model.registry.model.ModelRegistryEntity;
import org.molgenis.model.registry.model.ModelRegistryTag;
import org.molgenis.model.registry.utils.ModelRegistryServiceConfig;
import org.molgenis.model.registry.utils.ModelRegistryTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;

/**
 * @author sido
 */
@ContextConfiguration(classes = ModelRegistryServiceConfig.class)
public class MetaDataSearchServiceTest extends AbstractMolgenisSpringTest
{

	@Autowired
	private EntityTestHarness entityTestHarness;
	@Autowired
	private MetaDataService metaDataService;
	@Autowired
	private DataService dataService;
	@Autowired
	private TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private ModelRegistryTestHarness modelRegistryTestHarness;
	@Autowired
	private MetaDataSearchService metaDataSearchService;

	@Test
	public void testGetEntitiesInPackage()
	{
		String TEST_PACKAGE = "test-package";
		EntityType e1 = modelRegistryTestHarness.createEntityType(false);
		Package pkg = packageFactory.create(TEST_PACKAGE);
		pkg.set(PackageMetadata.ENTITY_TYPES, Lists.newArrayList(e1));

		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(pkg);

		List<ModelRegistryEntity> standardRegistryEntities = metaDataSearchService.getEntitiesInPackage(
				TEST_PACKAGE);
		assertEquals(1, standardRegistryEntities.size());
	}

	@Test
	public void testGetTagsForPackage()
	{
		String TEST_PACKAGE = "test-package";
		Package pkg = packageFactory.create(TEST_PACKAGE);
		Iterable<SemanticTag<Package, LabeledResource, LabeledResource>> semanticTags = Lists.newArrayList(
				modelRegistryTestHarness.createSemanticTag(pkg));
		when(tagService.getTagsForPackage(pkg)).thenReturn(semanticTags);
		List<ModelRegistryTag> tags = metaDataSearchService.getTagsForPackage(pkg);
		assertEquals(1, tags.size());
	}

	@Test
	public void testSearch()
	{
		String TEST_QUERY = "test-query";
		String TEST_PACKAGE = "test-package";
		String TEST_DESCRIPTION = "test-description";
		String TEST_LABEL = "test-label";

		PackageSearchRequest request = new PackageSearchRequest();
		request.setQuery(TEST_QUERY);

		Package pkg = packageFactory.create(TEST_PACKAGE, TEST_DESCRIPTION);
		EntityType refEntityTypeDynamic1 = entityTestHarness.createDynamicRefEntityType();
		EntityType entityTypeDynamic1 = entityTestHarness.createDynamicTestEntityType(refEntityTypeDynamic1);
		EntityType refEntityTypeDynamic2 = entityTestHarness.createDynamicRefEntityType();
		EntityType entityTypeDynamic2 = entityTestHarness.createDynamicTestEntityType(refEntityTypeDynamic2);
		entityTypeDynamic2.setAbstract(true);

		pkg.set(PackageMetadata.ENTITY_TYPES, Lists.newArrayList(entityTypeDynamic1, entityTypeDynamic2));
		Iterable<Package> packages = Lists.newArrayList(pkg);

		// metadataservice mocks
		when(metaDataService.getRootPackages()).thenReturn(packages);
		when(metaDataService.getPackage(TEST_PACKAGE)).thenReturn(pkg);

		// dataservice mocks
		Query<Package> packageQuery = new QueryImpl<Package>().search(TEST_QUERY);
		Stream<Package> packageStream = Stream.of(packageFactory.create(TEST_PACKAGE, TEST_LABEL));
		when(dataService.findAll(PackageMetadata.PACKAGE, packageQuery, Package.class)).thenReturn(packageStream);

		Query<EntityType> entityTypeQuery = new QueryImpl<EntityType>().search(TEST_QUERY);
		Stream<EntityType> entityStream = Stream.of(modelRegistryTestHarness.createEntityType(false));
		when(dataService.findAll(ENTITY_TYPE_META_DATA, entityTypeQuery, EntityType.class)).thenReturn(entityStream);

		// tagservice mocks
		Iterable<SemanticTag<Package, LabeledResource, LabeledResource>> symanticTags = Lists.newArrayList(
				modelRegistryTestHarness.createSemanticTag(pkg));
		when(tagService.getTagsForPackage(pkg)).thenReturn(symanticTags);

		ModelRegistrySearch response = metaDataSearchService.search(TEST_QUERY, 0, 3);

		assertEquals("test-package", response.getPackages().get(0).getName());

	}

}
