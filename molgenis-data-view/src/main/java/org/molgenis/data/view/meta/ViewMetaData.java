package org.molgenis.data.view.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class ViewMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "View";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String MASTER_ENTITY = "masterEntity";
	public static final String SLAVE_ENTITIES = "slaveEntities";

	public ViewMetaData()
	{
		super(ENTITY_NAME);

		setDescription("Contains the relationships between the master entity and each joined entity of a view.");

		addAttribute(IDENTIFIER, AttributeRole.ROLE_ID).setAuto(true).setNillable(false).setDataType(STRING);
		addAttribute(NAME).setNillable(false).setDataType(STRING).isUnique();
		addAttribute(MASTER_ENTITY).setNillable(false).setDataType(STRING);
		addAttribute(SLAVE_ENTITIES).setNillable(true).setDataType(MREF).setRefEntity(new SlaveEntityMetaData());
	}
}
