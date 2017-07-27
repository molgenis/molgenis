package org.molgenis.model.registry.model;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class ModelRegistryTreeNode
{

	public static ModelRegistryTreeNode create(String extraClasses, String title, String key, String tooltip,
			boolean folder, boolean expanded, Map<String, Object> data, List<ModelRegistryTreeNode> children)
	{
		return new AutoValue_ModelRegistryTreeNode(extraClasses, title, key, tooltip, folder, expanded, data, children);
	}

	public abstract String getExtraClasses();

	public abstract String getTitle();

	public abstract String getKey();

	@Nullable
	public abstract String getTooltip();

	public abstract boolean isFolder();

	public abstract boolean isExpanded();

	@Nullable
	public abstract Map<String, Object> getData();

	@Nullable
	public abstract List<ModelRegistryTreeNode> getChildren();

}
