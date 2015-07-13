package org.molgenis.data.mapper.meta;

import java.util.Arrays;

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

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(TARGETATTRIBUTEMETADATA).setNillable(false);
		addAttribute(SOURCEATTRIBUTEMETADATAS);
		addAttribute(ALGORITHM);
		EnumField enumField = new EnumField();
		enumField.setEnumOptions(Arrays.asList(AlgorithmState.GENRATED.toString(), AlgorithmState.CURATED.toString()));
		addAttribute(ALGORITHMSTATE).setDataType(enumField);
	}
}
