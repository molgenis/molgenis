package org.molgenis.data.view;

import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class EntityViewMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "EntityView";
	public static final String IDENTIFIER = "id";
	public static final String VIEW_NAME = "viewName";
	public static final String MASTER_ENTITY = "master_entity";
	public static final String MASTER_ATTR = "master_attr";
	public static final String JOIN_ENTITY = "join_entity";
	public static final String JOIN_ATTR = "join_attr";

	public EntityViewMetaData()
	{
		super(ENTITY_NAME);

		setDescription("Contains the relationships between the master entity and each joined entity of a view.");

		addAttribute(IDENTIFIER, AttributeRole.ROLE_ID).setAuto(true).setNillable(false).setDataType(STRING);
		addAttribute(VIEW_NAME).setNillable(false).setDataType(STRING);
		addAttribute(MASTER_ENTITY).setNillable(false).setDataType(STRING);
		addAttribute(MASTER_ATTR).setNillable(false).setDataType(STRING);
		addAttribute(JOIN_ENTITY).setNillable(false).setDataType(STRING);
		addAttribute(JOIN_ATTR).setNillable(false).setDataType(STRING);
	}
}
