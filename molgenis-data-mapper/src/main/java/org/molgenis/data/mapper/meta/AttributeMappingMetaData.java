package org.molgenis.data.mapper.meta;

import org.molgenis.data.mapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.ENUM;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

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
		setLabel("Attribute mapping");
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(TARGETATTRIBUTEMETADATA).setNillable(false);
		addAttribute(SOURCEATTRIBUTEMETADATAS).setDataType(TEXT);
		addAttribute(ALGORITHM).setDataType(TEXT);
		List<String> options = asList(AlgorithmState.values()).stream().map(AlgorithmState::toString).collect(toList());
		addAttribute(ALGORITHMSTATE).setDataType(ENUM).setEnumOptions(options);
	}
}
