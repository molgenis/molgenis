package org.molgenis.ontology.sorta.bean;

import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;

public class OntologyTermHitEntity extends MapEntity
{
	private static final long serialVersionUID = 428705681838535084L;

	public OntologyTermHitEntity(Entity entity, DefaultEntityMetaData entityMetaData)
	{
		super(entity, entityMetaData);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MapEntity other = (MapEntity) obj;
		return getIdValue().equals(other.getIdValue());
	}
}
