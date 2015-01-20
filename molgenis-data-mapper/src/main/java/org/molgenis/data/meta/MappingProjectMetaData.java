package org.molgenis.data.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.MREF;

@Component
public class MappingProjectMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MappingProject";
	public static final String IDENTIFIER = "identifier";
	public static final String OWNER = "owner";
	public static final String ENTITYMAPPINGS = "entityMappings";

	@SuppressWarnings("unused")
	private final DataService dataService;
	
	@Autowired
	public MappingProjectMetaData(DataService dataService)
	{
		super(ENTITY_NAME);
		this.dataService = dataService;
		
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		// FIXME use xref to MolgenisUser when https://github.com/molgenis/molgenis/issues/2054 is fixed
		addAttribute(OWNER).setNillable(false).setUnique(true);
		addAttribute(ENTITYMAPPINGS).setDataType(MREF).setRefEntity(EntityMappingRepository.META_DATA); 
	}
}
