package org.molgenis.data.view.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class SlaveEntityMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "SlaveEntity";
	public static final String IDENTIFIER = "identifier";
	public static final String SLAVE_ENTITY = "slaveEntity";
	public static final String JOINED_ATTRIBUTES = "joinedAttributes";

	public SlaveEntityMetaData()
	{
		super(ENTITY_NAME);
				
		addAttribute(IDENTIFIER, AttributeRole.ROLE_ID).setAuto(true).setNillable(false).setDataType(STRING);
		addAttribute(SLAVE_ENTITY).setNillable(false).setDataType(STRING).isUnique();
		addAttribute(JOINED_ATTRIBUTES).setNillable(false).setDataType(MREF)
				.setRefEntity(new JoinedAttributeMetaData());
	}
}
