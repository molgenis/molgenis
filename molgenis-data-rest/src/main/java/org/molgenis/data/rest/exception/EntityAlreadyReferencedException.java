package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import static java.util.Objects.requireNonNull;

public class EntityAlreadyReferencedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "R11";
	private final transient Entity refEntity;
	private final transient Attribute refAttribute;
	private final transient EntityType entityType;

	public EntityAlreadyReferencedException(Entity refEntity, Attribute refAttribute, EntityType entityType)
	{
		super(ERROR_CODE);
		this.refEntity = requireNonNull(refEntity);
		this.refAttribute = requireNonNull(refAttribute);
		this.entityType = requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("refEntity:%s refAttribute:%s entityType:%s", refEntity, refAttribute, entityType);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { refEntity, refAttribute, entityType };
	}
}
