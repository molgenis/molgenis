package org.molgenis.data.mapper.meta;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.stereotype.Component;

@Component
public class AttributeMappingMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "AttributeMapping";
	public static final String IDENTIFIER = "identifier";
	public static final String TARGETATTRIBUTEMETADATA = "targetAttributeMetaData";
	public static final String SOURCEATTRIBUTEMETADATAS = "sourceAttributeMetaDatas";
	public static final String ALGORITHM = "algorithm";
	public static final String ALGORITHMSTATE = "algorithmState";

	public AttributeMappingMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(TARGETATTRIBUTEMETADATA).setNillable(false);
		addAttribute(SOURCEATTRIBUTEMETADATAS);
		addAttribute(ALGORITHM).setDataType(MolgenisFieldTypes.TEXT);
		EnumField enumField = new EnumField();
		enumField.setEnumOptions(Arrays.asList(AlgorithmState.values()).stream().map(STATE -> STATE.toString())
				.collect(Collectors.toList()));
		addAttribute(ALGORITHMSTATE).setDataType(enumField);
	}
}
