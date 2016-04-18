package org.molgenis.ontology.ic;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class TermFrequencyEntityMetaData extends DefaultEntityMetaData
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
		addAttributeMetaData(new DefaultAttributeMetaData(ID).setAuto(true), ROLE_ID);
		addAttributeMetaData(new DefaultAttributeMetaData(TERM).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(FREQUENCY, FieldTypeEnum.INT).setNillable(false));
		addAttributeMetaData(new DefaultAttributeMetaData(OCCURRENCE, FieldTypeEnum.DECIMAL).setNillable(false));
	}
}