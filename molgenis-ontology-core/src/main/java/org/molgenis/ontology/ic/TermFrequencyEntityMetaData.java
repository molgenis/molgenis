package org.molgenis.ontology.ic;

import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class TermFrequencyEntityMetaData extends SystemEntityMetaDataImpl
{
	public final static String ENTITY_NAME = "TermFrequency";
	public final static String ID = "id";
	public final static String TERM = "term";
	public final static String FREQUENCY = "frequency";
	public final static String OCCURRENCE = "occurrence";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(ID, ROLE_ID).setAuto(true);
		addAttribute(TERM).setNillable(false);
		addAttribute(FREQUENCY).setDataType(INT).setNillable(false);
		addAttribute(OCCURRENCE).setDataType(DECIMAL).setNillable(false);
	}
}