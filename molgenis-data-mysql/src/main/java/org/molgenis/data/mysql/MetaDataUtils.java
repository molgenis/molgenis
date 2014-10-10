package org.molgenis.data.mysql;

import static org.molgenis.data.mysql.EntityMetaDataMetaData.ABSTRACT;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.DESCRIPTION;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.EXTENDS;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.FULL_NAME;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.ID_ATTRIBUTE;
import static org.molgenis.data.mysql.EntityMetaDataMetaData.LABEL;

import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;

public class MetaDataUtils
{
	public static DefaultEntityMetaData toEntityMetaData(Entity entity)
	{
		String name = entity.getString(FULL_NAME);
		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(name);
		entityMetaData.setAbstract(entity.getBoolean(ABSTRACT));
		entityMetaData.setIdAttribute(entity.getString(ID_ATTRIBUTE));
		entityMetaData.setLabel(entity.getString(LABEL));
		entityMetaData.setDescription(entity.getString(DESCRIPTION));

		// Extends
		Entity extendsEntity = entity.getEntity(EXTENDS);
		if (extendsEntity != null)
		{
			entityMetaData.setExtends(toEntityMetaData(extendsEntity));
		}

		return entityMetaData;
	}

}
