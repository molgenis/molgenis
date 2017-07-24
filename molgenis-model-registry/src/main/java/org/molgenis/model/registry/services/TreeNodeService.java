package org.molgenis.model.registry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.model.registry.model.ModelRegistryTreeNode;

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
	 * @return {@link ModelRegistryTreeNode}
	 */
	ModelRegistryTreeNode createTreeNode(Package package_);
}
