package org.molgenis.data;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;

public class RowLevelSecurityUtils
{
	/**
	 * Removes the _UPDATE permission attribute from row level secured EntityMetaData's.
	 *
	 * @param entityMetaData the EntityMetaData to remove the _UPDATE attribute from if it is row level secured
	 * @return EntityMetaData without the _UPDATE attribute if it is row level secured, or the input EntityMetaData if it is not
	 */
	public static EntityMetaData removeUpdateAttributeIfRowLevelSecured(EntityMetaData entityMetaData)
	{
		if (entityMetaData.isRowLevelSecured())
		{
			DefaultEntityMetaData filteredEntityMetaData = new DefaultEntityMetaData(entityMetaData);
			filteredEntityMetaData.removeAttributeMetaData(new DefaultAttributeMetaData("_UPDATE"));
			return filteredEntityMetaData;
		}
		else
		{
			return entityMetaData;
		}
	}
}
