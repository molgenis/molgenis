package org.molgenis.data.view.meta;

import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class JoinedAttributeMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "JoinedAttribute";
	public static final String IDENTIFIER = "identifier";
	public static final String MASTER_ATTRIBUTE = "masterAttribute";
	public static final String JOIN_ATTRIBUTE = "joinAttribute";

	public JoinedAttributeMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER, AttributeRole.ROLE_ID).setAuto(true).setNillable(false).setDataType(STRING);
		addAttribute(MASTER_ATTRIBUTE).setNillable(false).setDataType(STRING);
		addAttribute(JOIN_ATTRIBUTE).setNillable(false).setDataType(STRING);
	}
}
