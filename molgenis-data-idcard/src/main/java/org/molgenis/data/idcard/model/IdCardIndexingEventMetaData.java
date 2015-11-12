package org.molgenis.data.idcard.model;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;

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
		addAttribute(IdCardIndexingEvent.ID).setIdAttribute(true).setNillable(false).setVisible(false).setAuto(true)
				.setLabel("Id");
		addAttribute(IdCardIndexingEvent.DATE).setDataType(DATETIME).setNillable(false).setAuto(true).setLabel("Date");
		addAttribute(IdCardIndexingEvent.STATUS).setDataType(new EnumField())
				.setEnumOptions(Arrays.stream(IdCardIndexingEventStatus.values()).map(value -> value.toString())
						.collect(Collectors.toList()))
				.setNillable(false).setLabel("Status").setLabelAttribute(true).setLookupAttribute(true);
		addAttribute(IdCardIndexingEvent.MESSAGE).setDataType(TEXT).setNillable(true).setLabel("Message")
				.setLookupAttribute(true);
	}
}
