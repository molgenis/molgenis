package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.model.Attribute;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { mappingTargetAttribute, mappingTargetAttribute.getDataType().name(),
				mappingTargetAttribute.getRefEntity(), targetRepositoryAttribute,
				targetRepositoryAttribute.getDataType().name(), targetRepositoryAttribute.getRefEntity() };
	}
}