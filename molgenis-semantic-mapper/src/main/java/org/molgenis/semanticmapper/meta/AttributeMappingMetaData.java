package org.molgenis.semanticmapper.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.semanticmapper.meta.MapperPackage.PACKAGE_MAPPER;

@Component
public class AttributeMappingMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "AttributeMapping";
	public static final String ATTRIBUTE_MAPPING = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String TARGET_ATTRIBUTE = "targetAttribute";
	public static final String SOURCE_ATTRIBUTES = "sourceAttributes";
	public static final String ALGORITHM = "algorithm";
	public static final String ALGORITHM_STATE = "algorithmState";

	private final MapperPackage mapperPackage;

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
		addAttribute(TARGET_ATTRIBUTE).setNillable(false);
		addAttribute(SOURCE_ATTRIBUTES).setDataType(TEXT);
		addAttribute(ALGORITHM).setDataType(TEXT);
		List<String> options = asList(AlgorithmState.values()).stream().map(AlgorithmState::toString).collect(toList());
		addAttribute(ALGORITHM_STATE).setDataType(ENUM).setEnumOptions(options);
	}
}
