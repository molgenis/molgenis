package org.molgenis.model.registry.utils;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.model.registry.model.ModelRegistryTreeNode;
import org.molgenis.model.registry.model.ModelRegistryEntity;
import org.molgenis.model.registry.model.ModelRegistryTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author sido
 */
@Component
public class ModelRegistryTestHarness
{

	public final static String TEST_ENTITY_TYPE_NAME = "test-entity-type";
	public final static String TEST_TAG_NAME = "test-tag";
	public final static String TEST_IRI_NAME = "test-iri";

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	/**
	 *
	 * <p>Create specific {@link ModelRegistryEntity}-objects</p>
	 *
	 * @return {@link ModelRegistryEntity}
	 */
	public ModelRegistryEntity createStandardRegistryEntity() {
		ModelRegistryEntity entity = ModelRegistryEntity.create("test-entity", "test-description", false);
		return entity;
	}

	/**
	 *
	 * <p>Create {@link ModelRegistryTag}-object</p>
	 *
	 * @return {@link ModelRegistryTag}
	 */
	public ModelRegistryTag createStandardRegistryTag() {
		return ModelRegistryTag.create("test-tag", "", "");
	}

	/**
	 *
	 * <p>Create a {@link ModelRegistryTreeNode}</p>
	 *
	 * @return {@link ModelRegistryTreeNode}
	 */
	public ModelRegistryTreeNode createPackageTreeNode() {
		return ModelRegistryTreeNode.create("test-package", "", "", "", true, false, null, null);
	}

	/**
	 *
	 * <p>Create normal entityTypes.</p>
	 *
	 *
	 * @return {@link EntityType}
	 */
	public EntityType createEntityType(boolean isAbstract)
	{
		EntityType e1 = entityTypeFactory.create(TEST_ENTITY_TYPE_NAME);
		e1.setLabel("test-label");
		e1.setDescription("test-description");
		e1.setAbstract(isAbstract);
		return e1;
	}

	/**
	 *
	 * <p>Create {@link SemanticTag}-object.</p>
	 *
	 *
	 * @param pkg
	 * @return {@link SemanticTag}
	 */
	public SemanticTag<Package, LabeledResource, LabeledResource> createSemanticTag(Package pkg)
	{
		LabeledResource objectType = new LabeledResource(TEST_IRI_NAME, "test-object-label");
		LabeledResource codeSystemType = new LabeledResource("test-iri-code-system", "test-code-system-label");
		SemanticTag<Package, LabeledResource, LabeledResource> tag = new SemanticTag<>("test", pkg, Relation.instanceOf,
				objectType, codeSystemType);
		return tag;
	}

}
