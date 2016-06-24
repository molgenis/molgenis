package org.molgenis.data.idcard.model;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.idcard.model.IdCardPackage.PACKAGE_ID_CARD;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdCardIndexingEventMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "IdCardIndexingEvent";
	public static final String ID_CARD_INDEXING_EVENT = PACKAGE_ID_CARD + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String DATE = "date";
	public static final String STATUS = "status";
	public static final String MESSAGE = "message";

	private final IdCardPackage idCardPackage;

	@Autowired
	IdCardIndexingEventMetaData(IdCardPackage idCardPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ID_CARD);
		this.idCardPackage = requireNonNull(idCardPackage);
	}

	@Override
	public void init()
	{
		setPackage(idCardPackage);

		addAttribute(ID, ROLE_ID).setVisible(false).setAuto(true).setLabel("Id");
		addAttribute(DATE).setDataType(DATETIME).setNillable(false).setAuto(true).setLabel("Date");
		addAttribute(STATUS, ROLE_LABEL, ROLE_LOOKUP).setDataType(new EnumField()).setEnumOptions(
				Arrays.stream(IdCardIndexingEventStatus.values()).map(value -> value.toString())
						.collect(Collectors.toList())).setNillable(false).setLabel("Status");
		addAttribute(MESSAGE, ROLE_LOOKUP).setDataType(TEXT).setNillable(true).setLabel("Message");
	}
}
