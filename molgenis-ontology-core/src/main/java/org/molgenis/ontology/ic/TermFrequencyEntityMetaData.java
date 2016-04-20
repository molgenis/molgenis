package org.molgenis.ontology.ic;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class TermFrequencyEntityMetaData extends EntityMetaData
{
	public final static String ENTITY_NAME = "TermFrequency";
	public final static String ID = "id";
	public final static String TERM = "term";
	public final static String FREQUENCY = "frequency";
	public final static String OCCURRENCE = "occurrence";
	public final static TermFrequencyEntityMetaData INSTANCE = new TermFrequencyEntityMetaData();

	public TermFrequencyEntityMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(new AttributeMetaData(ID).setAuto(true), ROLE_ID);
		addAttribute(new AttributeMetaData(TERM).setNillable(false));
		addAttribute(new AttributeMetaData(FREQUENCY, FieldTypeEnum.INT).setNillable(false));
		addAttribute(new AttributeMetaData(OCCURRENCE, FieldTypeEnum.DECIMAL).setNillable(false));
	}
}