package org.molgenis.ontology.sorta.bean;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;

public class OntologyTermHitEntity extends DynamicEntity
{
	private static final long serialVersionUID = 428705681838535084L;

	public OntologyTermHitEntity(Entity entity, EntityType entityType)
	{
		super(entityType);
		set(entity);
	}

	protected void validateValueType(String attrName, Object value)
	{
		// no operation
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DynamicEntity other = (DynamicEntity) obj;
		return getIdValue().equals(other.getIdValue());
	}

	@Override
	public int hashCode()
	{
		Object idValue = getIdValue();
		return idValue != null ? getIdValue().hashCode() : 0;
	}
}
