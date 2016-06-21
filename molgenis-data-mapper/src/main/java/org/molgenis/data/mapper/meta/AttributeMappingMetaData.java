package org.molgenis.data.mapper.meta;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.molgenis.fieldtypes.EnumField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AttributeMappingMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "AttributeMapping";
	public static final String ATTRIBUTE_MAPPING = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String TARGETATTRIBUTEMETADATA = "targetAttributeMetaData";
	public static final String SOURCEATTRIBUTEMETADATAS = "sourceAttributeMetaDatas";
	public static final String ALGORITHM = "algorithm";
	public static final String ALGORITHMSTATE = "algorithmState";

	private final MapperPackage mapperPackage;

	@Autowired
	public AttributeMappingMetaData(MapperPackage mapperPackage)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
	}

	@Override
	public void init()
	{
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(TARGETATTRIBUTEMETADATA).setNillable(false);
		addAttribute(SOURCEATTRIBUTEMETADATAS);
		addAttribute(ALGORITHM).setDataType(MolgenisFieldTypes.TEXT);
		List<String> algorithmStateOptions = asList(AlgorithmState.values()).stream().map(STATE -> STATE.toString())
				.collect(toList());
		EnumField enumField = new EnumField();
		enumField.setEnumOptions(algorithmStateOptions); // FIXME remove this hack
		addAttribute(ALGORITHMSTATE).setDataType(enumField).setEnumOptions(algorithmStateOptions);
	}
}
