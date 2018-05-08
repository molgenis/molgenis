package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

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
	public abstract EditorEntityTypeParent getEntityTypeParent();

	public abstract List<EditorAttribute> getAttributes();

	public abstract List<EditorAttributeIdentifier> getReferringAttributes();

	public abstract List<EditorTagIdentifier> getTags();

	@Nullable
	public abstract EditorAttributeIdentifier getIdAttribute();

	@Nullable
	public abstract EditorAttributeIdentifier getLabelAttribute();

	public abstract List<EditorAttributeIdentifier> getLookupAttributes();

	public static EditorEntityType create(String id, @Nullable String label, Map<String, String> i18nLabel,
			@Nullable String description, Map<String, String> i18nDescription, boolean abstract_, String backend,
			@Nullable EditorPackageIdentifier package_, @Nullable EditorEntityTypeParent entityTypeParent,
			List<EditorAttribute> attributes, List<EditorAttributeIdentifier> referringAttributes, List<EditorTagIdentifier> tags, @Nullable EditorAttributeIdentifier idAttribute,
			@Nullable EditorAttributeIdentifier labelAttribute, List<EditorAttributeIdentifier> lookupAttributes)
	{
		return new AutoValue_EditorEntityType(id, label, i18nLabel, description, i18nDescription, abstract_, backend,
				package_, entityTypeParent, attributes, referringAttributes, tags, idAttribute, labelAttribute, lookupAttributes);
	}
}
