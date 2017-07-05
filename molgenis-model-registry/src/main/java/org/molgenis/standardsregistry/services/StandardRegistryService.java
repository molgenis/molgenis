package org.molgenis.standardsregistry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.standardsregistry.model.*;

import java.util.List;

/**
 *
 * <p>Service to create responses for model registry.</p>
 *
 * @author sido
 */
public interface StandardRegistryService {



    /**
     *
     * <p>Builds the node in the packages-tree</p>
     *
     * @param package_ {@link Package}
     *
     * @return {@link PackageTreeNode}
     */
    PackageTreeNode createPackageTreeNode(Package package_);
}
