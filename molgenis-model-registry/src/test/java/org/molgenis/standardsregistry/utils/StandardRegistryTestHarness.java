package org.molgenis.standardsregistry.utils;

import com.google.common.collect.Lists;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.standardsregistry.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author sido
 */
@Component
public class StandardRegistryTestHarness {

    public final static String TEST_ENTITY_TYPE_NAME = "test-entity-type";
    public final static String TEST_TAG_NAME = "test-tag";
    public final static String TEST_IRI_NAME = "test-iri";


    @Autowired
    private EntityTypeFactory entityTypeFactory;

    /**
     *
     * <p>Creates a list of {@link StandardRegistryEntity}-objects</p>
     *
     * @return
     */
    public List<StandardRegistryEntity> createStandardRegistryEntities() {
        List<StandardRegistryEntity> entities = new ArrayList<>();
        entities.add(createStandardRegistryEntity());
        entities.add(createStandardRegistryEntity());
        return entities;
    }

    /**
     * <p>Creates a list of {@link StandardRegistryTag}-objects</p>
     *
     * @return
     */
    public List<StandardRegistryTag> createStandardRegistryTags() {
        List<StandardRegistryTag> entities = new ArrayList<>();
        entities.add(createStandardRegistryTag());
        entities.add(createStandardRegistryTag());
        return entities;
    }

    /**
     *
     * @return
     */
    public EntityType createEntityType(boolean isAbstract) {
        EntityType e1 = entityTypeFactory.create(TEST_ENTITY_TYPE_NAME);
        e1.setLabel("test-label");
        e1.setAbstract(isAbstract);
        return e1;
    }

    public Iterable<SemanticTag<Package, LabeledResource, LabeledResource>> createSemanticTagList(Package pkg) {
        LabeledResource objectType = new LabeledResource(TEST_IRI_NAME, "test-object-label");
        LabeledResource codeSystemType = new LabeledResource("test-iri-code-system", "test-code-system-label");
        SemanticTag<Package, LabeledResource, LabeledResource> tag = new SemanticTag<>("test", pkg, Relation.instanceOf, objectType, codeSystemType);
        return Lists.newArrayList(tag);
    }

    /**
     *
     *
     *
     * @return
     */
    public PackageTreeNode createPackageTreeNode() {
        return new PackageTreeNode("", "testTreeNode", "", "", false, false, new HashMap<>());
    }

    /**
     *
     * @return
     */
    private StandardRegistryTag createStandardRegistryTag() {
        return new StandardRegistryTag(TEST_TAG_NAME, TEST_IRI_NAME, "testRelation");
    }

    /**
     *
     * @return
     */
    private StandardRegistryEntity createStandardRegistryEntity() {
        return new StandardRegistryEntity(TEST_ENTITY_TYPE_NAME, "testLabel", false);
    }





}
