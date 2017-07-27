package org.molgenis.model.registry.services;

import org.molgenis.data.meta.model.Package;
import org.molgenis.model.registry.mappers.TreeNodeMapper;
import org.molgenis.model.registry.model.ModelRegistryTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <p>TreeNode service which provides treenodes.</p>
 *
 * @author sido
 */
@Service
public class TreeNodeServiceImpl implements TreeNodeService
{

	private final TreeNodeMapper treeNodeMapper;

	@Autowired
	public TreeNodeServiceImpl(TreeNodeMapper treeNodeMapper)
	{
		this.treeNodeMapper = Objects.requireNonNull(treeNodeMapper);
	}

	@Override
	public ModelRegistryTreeNode createTreeNode(Package package_)
	{
		return treeNodeMapper.fromPackageToTreeNode(package_);
	}

}
