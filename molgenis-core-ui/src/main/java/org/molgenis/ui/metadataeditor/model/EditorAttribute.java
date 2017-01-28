package org.molgenis.ui.metadataeditor.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttribute.class)
public abstract class EditorAttribute
{
	abstract String getId();

	abstract String getName();

	abstract String getType();

	@Nullable
	abstract EditorAttributeIdentifier getParent();

	@Nullable
	abstract EditorEntityTypeIdentifier getRefEntityType();

	@Nullable
	abstract EditorAttributeIdentifier getMappedByEntityType();

	@Nullable
	abstract EditorSort getOrderBy();

	@Nullable
	abstract String getExpression();

	abstract boolean isNullable();

	abstract boolean isAuto();

	abstract boolean isVisible();

	@Nullable
	abstract String getLabel();

	abstract ImmutableMap<String, String> getLabelI18n();

	@Nullable
	abstract String getDescription();

	abstract ImmutableMap<String, String> getDescriptionI18n();

	abstract boolean isAggregatable();

	@Nullable
	abstract ImmutableList<String> getEnumOptions();

	@Nullable
	abstract Long getRangeMin();

	@Nullable
	abstract Long getRangeMax();

	abstract boolean isReadonly();

	abstract boolean isUnique();

	abstract ImmutableList<EditorTag> getTags();

	@Nullable
	abstract String getVisibleExpression();

	@Nullable
	abstract String getValidationExpression();

	@Nullable
	abstract String getDefaultValue();

	public static EditorAttribute create(String id, String name, String type, EditorAttributeIdentifier parent,
			EditorEntityTypeIdentifier refEntityType, EditorAttributeIdentifier mappedByEntityType, EditorSort orderBy,
			String expression, boolean nullable, boolean auto, boolean visible, @Nullable String label,
			ImmutableMap<String, String> i18nLabel, @Nullable String description,
			ImmutableMap<String, String> i18nDescription, boolean aggregatable, ImmutableList<String> enumOptions,
			Long rangeMin, Long rangeMax, boolean readonly, boolean unique, ImmutableList<EditorTag> tags,
			String visibleExpression, String validationExpression, String defaultValue)
	{
		return new AutoValue_EditorAttribute(id, name, type, parent, refEntityType, mappedByEntityType, orderBy,
				expression, nullable, auto, visible, label, i18nLabel, description, i18nDescription, aggregatable,
				enumOptions, rangeMin, rangeMax, readonly, unique, tags, visibleExpression, validationExpression,
				defaultValue);
	}
}
