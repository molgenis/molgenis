package org.molgenis.model.registry.mappers;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.model.registry.model.ModelRegistryTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <p>Build treenodes for packagetree in ModelRegistry.</p>
 *
 * @author sido
 */
@Component
public class TreeNodeMapper
{

	private final TagService<LabeledResource, LabeledResource> tagService;

	@Autowired
	public TreeNodeMapper(TagService<LabeledResource, LabeledResource> tagService)
	{
		this.tagService = Objects.requireNonNull(tagService);
	}

	public ModelRegistryTreeNode fromPackageToTreeNode(Package package_)
	{
		String title = package_.getLabel() != null ? package_.getLabel() : package_.getId();
		String key = package_.getId();
		String tooltip = package_.getDescription();
		List<ModelRegistryTreeNode> result = new ArrayList<>();
		boolean folder = true;
		boolean expanded = true;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "package");

		for (Package subPackage : package_.getChildren())
		{
			result.add(fromPackageToTreeNode(subPackage));
		}

		for (EntityType emd : package_.getEntityTypes())
		{
			result.add(fromEntityTypeToTreeNode(emd));
		}

		return ModelRegistryTreeNode.create("package", title, key, tooltip, folder, expanded, data, result);
	}

	private ModelRegistryTreeNode fromEntityTypeToTreeNode(EntityType emd)
	{
		String title = emd.getLabel();
		String key = emd.getId();
		String tooltip = emd.getDescription();
		List<ModelRegistryTreeNode> result = new ArrayList<>();
		boolean folder = true;
		boolean expanded = false;

		Map<String, Object> data = new HashMap<>();
		data.put("type", "entity");
		data.put("href", "/api/v1/" + emd.getId() + "/meta");

		for (Attribute amd : emd.getAttributes())
		{
			result.add(fromAttributeTreeNode(amd, emd));
		}

		return ModelRegistryTreeNode.create("entity", title, key, tooltip, folder, expanded, data, result);
	}

	private ModelRegistryTreeNode fromAttributeTreeNode(Attribute amd, EntityType emd)
	{
		String title = amd.getLabel();
		String key = amd.getName();
		String tooltip = amd.getDescription();
		List<ModelRegistryTreeNode> result = new ArrayList<>();
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
				result.add(fromAttributeTreeNode(subAmd, emd));
			}
			folder = true;
		}
		else
		{
			folder = false;
		}

		return ModelRegistryTreeNode.create("attribute", title, key, tooltip, folder, expanded, data, result);
	}
	
}
