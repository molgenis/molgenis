package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorAttribute.class)
public abstract class EditorAttribute
{
	public abstract String getId();

	@Nullable
	public abstract String getName();

	@Nullable
	public abstract String getType();

	@Nullable
	public abstract EditorAttributeIdentifier getParent();

	@Nullable
	public abstract EditorEntityTypeIdentifier getRefEntityType();

	@Nullable
	public abstract EditorAttributeIdentifier getMappedByAttribute();

	@Nullable
	public abstract EditorSort getOrderBy();

	@Nullable
	public abstract String getExpression();

	public abstract boolean isNullable();

	public abstract boolean isAuto();

	public abstract boolean isVisible();

	@Nullable
	public abstract String getLabel();

	public abstract Map<String, String> getLabelI18n();

	@Nullable
	public abstract String getDescription();

	public abstract Map<String, String> getDescriptionI18n();

	public abstract boolean isAggregatable();

	@Nullable
	public abstract List<String> getEnumOptions();

	@Nullable
	public abstract Long getRangeMin();

	@Nullable
	public abstract Long getRangeMax();

	public abstract boolean isReadonly();

	public abstract boolean isUnique();

	public abstract List<EditorTagIdentifier> getTags();

	@Nullable
	public abstract String getNullableExpression();

	@Nullable
	public abstract String getVisibleExpression();

	@Nullable
	public abstract String getValidationExpression();

	@Nullable
	public abstract String getDefaultValue();

	public abstract Integer getSequenceNumber();

	public static EditorAttribute create(String id, @Nullable String name, @Nullable String type,
			EditorAttributeIdentifier parent, EditorEntityTypeIdentifier refEntityType,
			EditorAttributeIdentifier mappedByAttribute, EditorSort orderBy, String expression, boolean nullable,
			boolean auto, boolean visible, @Nullable String label, Map<String, String> i18nLabel,
			@Nullable String description, Map<String, String> i18nDescription, boolean aggregatable,
			@Nullable List<String> enumOptions, @Nullable Long rangeMin, @Nullable Long rangeMax, boolean readonly,
			boolean unique, List<EditorTagIdentifier> tags, @Nullable String nullableExpression,
			@Nullable String visibleExpression,
			@Nullable String validationExpression, @Nullable String defaultValue, Integer sequenceNumber)
	{
		return new AutoValue_EditorAttribute(id, name, type, parent, refEntityType, mappedByAttribute, orderBy,
				expression, nullable, auto, visible, label, i18nLabel, description, i18nDescription, aggregatable,
				enumOptions, rangeMin, rangeMax, readonly, unique, tags, nullableExpression, visibleExpression,
				validationExpression,
				defaultValue, sequenceNumber);
	}
}
