package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityType.class)
public abstract class EditorEntityType
{
	abstract String getId();

	abstract String getName();

	@Nullable
	abstract String getLabel();

	abstract ImmutableMap<String, String> getLabelI18n();

	@Nullable
	abstract String getDescription();

	abstract ImmutableMap<String, String> getDescriptionI18n();

	abstract boolean isAbstract();

	abstract String getBackend();

	@Nullable
	abstract EditorPackage getPackage();

	@Nullable
	abstract EditorEntityTypeParent getParent();

	abstract ImmutableList<EditorEntityTypeIdentifier> getChildren();

	abstract ImmutableList<EditorAttribute> getAttributes();

	abstract ImmutableList<EditorTag> getTags();

	@Nullable
	abstract EditorAttributeIdentifier getIdAttribute();

	@Nullable
	abstract EditorAttributeIdentifier getLabelAttribute();

	abstract ImmutableList<EditorAttributeIdentifier> getLookupAttributes();

	public static EditorEntityType create(String id, String name, @Nullable String label,
			ImmutableMap<String, String> i18nLabel, @Nullable String description,
			ImmutableMap<String, String> i18nDescription, boolean abstract_, String backend, EditorPackage package_,
			@Nullable EditorEntityTypeParent entityTypeParent,
			ImmutableList<EditorEntityTypeIdentifier> entityTypeChildren, ImmutableList<EditorAttribute> attributes,
			ImmutableList<EditorTag> tags, EditorAttributeIdentifier idAttribute,
			EditorAttributeIdentifier labelAttribute, ImmutableList<EditorAttributeIdentifier> lookupAttributes)
	{
		return new AutoValue_EditorEntityType(id, name, label, i18nLabel, description, i18nDescription, abstract_,
				backend, package_, entityTypeParent, entityTypeChildren, attributes, tags, idAttribute, labelAttribute,
				lookupAttributes);
	}
}
