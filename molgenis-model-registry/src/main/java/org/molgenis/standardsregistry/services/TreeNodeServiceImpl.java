package org.molgenis.standardsregistry.services;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.standardsregistry.model.PackageTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p></p>
 *
 * @author sido
 */
@Service
public class TreeNodeServiceImpl implements TreeNodeService
{

	private final TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	public TreeNodeServiceImpl(TagService<LabeledResource, LabeledResource> tagService)
	{
		this.tagService = Objects.requireNonNull(tagService);
	}

	@Override
	public PackageTreeNode createPackageTreeNode(Package package_)
	{
		String title = package_.getLabel() != null ? package_.getLabel() : package_.getId();
		String key = package_.getId();
		String tooltip = package_.getDescription();
		List<PackageTreeNode> result = new ArrayList<>();
		boolean folder = true;
		boolean expanded = true;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "package");

		for (Package subPackage : package_.getChildren())
		{
			result.add(createPackageTreeNode(subPackage));
		}

		for (EntityType emd : package_.getEntityTypes())
		{
			result.add(createPackageTreeNode(emd));
		}

		return new PackageTreeNode("package", title, key, tooltip, folder, expanded, data, result);
	}

	private PackageTreeNode createPackageTreeNode(EntityType emd)
	{
		String title = emd.getLabel();
		String key = emd.getId();
		String tooltip = emd.getDescription();
		List<PackageTreeNode> result = new ArrayList<>();
		boolean folder = true;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "entity");
		data.put("href", "/api/v1/" + emd.getId() + "/meta");

		for (Attribute amd : emd.getAttributes())
		{
			result.add(createPackageTreeNode(amd, emd));
		}

		return new PackageTreeNode("entity", title, key, tooltip, folder, expanded, data, result);
	}

	private PackageTreeNode createPackageTreeNode(Attribute amd, EntityType emd)
	{
		String title = amd.getLabel();
		String key = amd.getName();
		String tooltip = amd.getDescription();
		List<PackageTreeNode> result = new ArrayList<>();
		boolean folder;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "attribute");
		data.put("href", "/api/v1/" + emd.getId() + "/meta/" + amd.getName());
		data.put("tags", tagService.getTagsForAttribute(emd, amd));

		if (amd.getDataType() == AttributeType.COMPOUND)
		{
			for (Attribute subAmd : amd.getChildren())
			{
				result.add(createPackageTreeNode(subAmd, emd));
			}
			folder = true;
		}
		else
		{
			folder = false;
		}

		return new PackageTreeNode("attribute", title, key, tooltip, folder, expanded, data, result);
	}

}
