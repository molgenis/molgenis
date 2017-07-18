package org.molgenis.standardsregistry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.standardsregistry.model.PackageTreeNode;

/**
 * <p>Service to create treenodes for model registry.</p>
 *
 * @author sido
 */
public interface TreeNodeService
{

	/**
	 * <p>Builds the node in the packages-tree</p>
	 *
	 * @param package_ {@link Package}
	 * @return {@link PackageTreeNode}
	 */
	PackageTreeNode createPackageTreeNode(Package package_);
}
