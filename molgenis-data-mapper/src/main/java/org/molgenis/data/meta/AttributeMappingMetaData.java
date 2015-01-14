package org.molgenis.data.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;

public class AttributeMappingMetaData extends DefaultEntityMetaData
{
	@Autowired
	private DataService dataService;

	public static final String ENTITY_NAME = "attributes";
	public static final String IDENTIFIER = "identifier";
	public static final String OWNER = "owner";
	public static final String ENTITYMAPPINGS = "entityMappings";

	public AttributeMappingMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(ENTITYMAPPINGS).setDataType(MREF).setRefEntity(EntityMappingRepository.META_DATA);
		addAttribute(OWNER).setDataType(XREF).setRefEntity(dataService.getEntityMetaData("MolgenisUser"));
	}
}
