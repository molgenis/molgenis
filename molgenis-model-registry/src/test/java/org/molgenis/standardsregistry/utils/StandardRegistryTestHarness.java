package org.molgenis.standardsregistry.utils;

import org.molgenis.standardsregistry.model.PackageTreeNode;
import org.molgenis.standardsregistry.model.StandardRegistryEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author sido
 */
@Component
public class StandardRegistryTestHarness {

    /**
     *
     * <p>Creates a list of {@link StandardRegistryEntity}-objects</p>
     *
     * @return
     */
    public List<StandardRegistryEntity> createStandardRegsitryEntities() {
        List<StandardRegistryEntity> entities = new ArrayList<>();
        entities.add(createStandardRegistryEntity());
        entities.add(createStandardRegistryEntity());
        return entities;
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
    private StandardRegistryEntity createStandardRegistryEntity() {
        StandardRegistryEntity standardRegistryEntity = new StandardRegistryEntity("testEntity", "testLabel", false);
        return standardRegistryEntity;
    }



}
