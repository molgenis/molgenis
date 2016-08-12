package org.molgenis.ontology.sorta.bean;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;

public class OntologyTermHitEntity extends DynamicEntity
{
	private static final long serialVersionUID = 428705681838535084L;

	public OntologyTermHitEntity(Entity entity, EntityMetaData entityMetaData)
	{
		super(entityMetaData);
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
}
