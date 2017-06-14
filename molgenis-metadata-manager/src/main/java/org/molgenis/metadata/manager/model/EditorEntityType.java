package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityType.class)
public abstract class EditorEntityType
{
	public abstract String getId();

	@Nullable
	public abstract String getLabel();

	public abstract Map<String, String> getLabelI18n();

	@Nullable
	public abstract String getDescription();

	public abstract Map<String, String> getDescriptionI18n();

	public abstract boolean isAbstract();

	public abstract String getBackend();

	@Nullable
	public abstract EditorPackageIdentifier getPackage();

	@Nullable
	public abstract EditorEntityTypeParent getParent();

	public abstract List<EditorAttribute> getAttributes();

	public abstract List<EditorTagIdentifier> getTags();

	@Nullable
	public abstract EditorAttributeIdentifier getIdAttribute();

	@Nullable
	public abstract EditorAttributeIdentifier getLabelAttribute();

	public abstract List<EditorAttributeIdentifier> getLookupAttributes();

	public static EditorEntityType create(String id, @Nullable String label, Map<String, String> i18nLabel,
			@Nullable String description, Map<String, String> i18nDescription, boolean abstract_, String backend,
			EditorPackageIdentifier package_, @Nullable EditorEntityTypeParent entityTypeParent,
			List<EditorAttribute> attributes, List<EditorTagIdentifier> tags, EditorAttributeIdentifier idAttribute,
			EditorAttributeIdentifier labelAttribute, List<EditorAttributeIdentifier> lookupAttributes)
	{
		return new AutoValue_EditorEntityType(id, label, i18nLabel, description, i18nDescription, abstract_, backend,
				package_, entityTypeParent, attributes, tags, idAttribute, labelAttribute, lookupAttributes);
	}
}
