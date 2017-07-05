package org.molgenis.standardsregistry.utils;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * @return
     */
    public EntityType createEntityType(boolean isAbstract) {
        EntityType e1 = entityTypeFactory.create(TEST_ENTITY_TYPE_NAME);
        e1.setLabel("test-label");
        e1.setAbstract(isAbstract);
        return e1;
    }

    public SemanticTag<Package, LabeledResource, LabeledResource> createSemanticTag(Package pkg) {
        LabeledResource objectType = new LabeledResource(TEST_IRI_NAME, "test-object-label");
        LabeledResource codeSystemType = new LabeledResource("test-iri-code-system", "test-code-system-label");
        SemanticTag<Package, LabeledResource, LabeledResource> tag = new SemanticTag<>("test", pkg, Relation.instanceOf, objectType, codeSystemType);
        return tag;
    }





}
