package org.molgenis.data.idcard.model;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.idcard.model.IdCardPackage.PACKAGE_ID_CARD;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class IdCardIndexingEventMetaData extends SystemEntityType
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
		setLabel("ID-Card indexing event");
		setPackage(idCardPackage);

		addAttribute(ID, ROLE_ID).setVisible(false).setAuto(true).setLabel("Id");
		addAttribute(DATE).setDataType(DATE_TIME).setNillable(false).setAuto(true).setLabel("Date");
		addAttribute(STATUS, ROLE_LABEL, ROLE_LOOKUP).setDataType(ENUM).setEnumOptions(
				Arrays.stream(IdCardIndexingEventStatus.values()).map(value -> value.toString())
						.collect(Collectors.toList())).setNillable(false).setLabel("Status");
		addAttribute(MESSAGE, ROLE_LOOKUP).setDataType(TEXT).setNillable(true).setLabel("Message");
	}
}
