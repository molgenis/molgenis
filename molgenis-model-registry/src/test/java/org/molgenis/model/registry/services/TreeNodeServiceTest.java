package org.molgenis.model.registry.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.TestHarnessConfig;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.model.registry.model.ModelRegistryTreeNode;
import org.molgenis.model.registry.utils.ModelRegistryTestHarness;
import org.molgenis.model.registry.utils.ModelRegistryServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author sido
 */
@ContextConfiguration(classes = { ModelRegistryServiceConfig.class, TestHarnessConfig.class })
public class TreeNodeServiceTest extends AbstractMolgenisSpringTest
{

	@Autowired
	private PackageFactory packageFactory;
	@Autowired
	private ModelRegistryTestHarness modelRegistryTestHarness;

	@Autowired
	private TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	private TreeNodeService treeNodeService;

	@Test
	public void testCreatePackageTreeNode()
	{
		String TEST_PACKAGE = "test-package";
		EntityType e1 = modelRegistryTestHarness.createEntityType(false);
		Package pkg = packageFactory.create(TEST_PACKAGE);
		pkg.set(PackageMetadata.ENTITY_TYPES, Lists.newArrayList(e1));

		Multimap<Relation, LabeledResource> tags = ArrayListMultimap.create();
		tags.put(Relation.hasLowerValue, new LabeledResource("test-id", "test-label"));

		pkg.getEntityTypes().forEach(entityType ->
		{
			when(tagService.getTagsForAttribute(pkg.getEntityType(), entityType.getIdAttribute())).thenReturn(tags);
		});

		ModelRegistryTreeNode modelRegistryTreeNode = treeNodeService.createTreeNode(pkg);
		assertEquals(true, modelRegistryTreeNode.getTitle().equalsIgnoreCase("test-package"));
	}

	@Test
	public void testPackageTreeNodeNotFound() {

	}

}
