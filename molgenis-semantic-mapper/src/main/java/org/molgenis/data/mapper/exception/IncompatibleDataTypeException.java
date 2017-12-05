package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.model.Attribute;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class IncompatibleDataTypeException extends IncompatibleTargetException
{
	private static final String ERROR_CODE = "M05";
	private final transient Attribute mappingTargetAttribute;
	private final transient Attribute targetRepositoryAttribute;

	public IncompatibleDataTypeException(Attribute mappingTargetAttribute, Attribute targetRepositoryAttribute)
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

	@Override
	public String getMessage()
	{
		return format("name:%s type:%s, targetName:%s targetType:%s", mappingTargetAttribute.getName(),
				mappingTargetAttribute.getDataType(), targetRepositoryAttribute.getName(),
				targetRepositoryAttribute.getDataType());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { mappingTargetAttribute, mappingTargetAttribute.getDataType().name(),
				targetRepositoryAttribute, targetRepositoryAttribute.getDataType().name() };
	}
}
