package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IncompatibleReferenceException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M06";
	private final transient Attribute mappingTargetAttribute;
	private final transient Attribute targetRepositoryAttribute;

	public IncompatibleReferenceException(Attribute mappingTargetAttribute, Attribute targetRepositoryAttribute)
	{
		super(ERROR_CODE);

		this.mappingTargetAttribute = requireNonNull(mappingTargetAttribute);
		this.targetRepositoryAttribute = requireNonNull(targetRepositoryAttribute);
	}

	public Attribute getMappingTargetAttribute()
	{
		return mappingTargetAttribute;
	}

	public Attribute getTargetRepositoryAttribute()
	{
		return targetRepositoryAttribute;
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public String getMessage()
	{
		return format("name:%s type:%s ref:%s, targetName:%s, targetType:%s, targetRef:%s",
				mappingTargetAttribute.getName(), mappingTargetAttribute.getDataType(),
				mappingTargetAttribute.getRefEntity().getId(), targetRepositoryAttribute.getName(),
				targetRepositoryAttribute.getDataType(), targetRepositoryAttribute.getRefEntity().getId());
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, mappingTargetAttribute.getLabel(language),
					mappingTargetAttribute.getDataType().name(),
					mappingTargetAttribute.getRefEntity().getLabel(language),
					targetRepositoryAttribute.getLabel(language), targetRepositoryAttribute.getDataType().name(),
					targetRepositoryAttribute.getRefEntity().getLabel(language));
		}).orElseGet(super::getLocalizedMessage);
	}
}