package org.molgenis.data.idcard.model;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.stereotype.Component;

@Component
public class IdCardIndexingEventMetaData extends DefaultEntityMetaData
{
	public IdCardIndexingEventMetaData()
	{
		super(IdCardIndexingEvent.ENTITY_NAME, IdCardIndexingEvent.class);
		addAttribute(IdCardIndexingEvent.ID, ROLE_ID).setVisible(false).setAuto(true).setLabel("Id");
		addAttribute(IdCardIndexingEvent.DATE).setDataType(DATETIME).setNillable(false).setAuto(true).setLabel("Date");
		addAttribute(IdCardIndexingEvent.STATUS, ROLE_LABEL, ROLE_LOOKUP)
				.setDataType(new EnumField()).setEnumOptions(Arrays.stream(IdCardIndexingEventStatus.values())
						.map(value -> value.toString()).collect(Collectors.toList()))
				.setNillable(false).setLabel("Status");
		addAttribute(IdCardIndexingEvent.MESSAGE, ROLE_LOOKUP).setDataType(TEXT).setNillable(true).setLabel("Message");
	}
}
