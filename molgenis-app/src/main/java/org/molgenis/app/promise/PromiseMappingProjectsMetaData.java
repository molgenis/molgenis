package org.molgenis.app.promise;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromiseMappingProjectsMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "PromiseMappingProjects";
	public static final String FULLY_QUALIFIED_NAME = PromisePackage.NAME + '_' + ENTITY_NAME;

	public static final String ID = "name";
	public static final String CREDENTIALS = "credentials";
	public static final String MAPPER = "mapper";

	@Autowired
	public PromiseMappingProjectsMetaData(DataService dataService)
	{
		super(ENTITY_NAME, PromisePackage.getPackage());

		setLabel("ProMISe mapping projects");
		setDescription("");

		EntityMetaData credentialsEntity = dataService.getMeta().getEntityMetaData(
				PromiseCredentialsMetaData.FULLY_QUALIFIED_NAME);

		addAttribute(ID).setIdAttribute(true).setNillable(false);
		addAttribute(CREDENTIALS).setDataType(MolgenisFieldTypes.MREF).setRefEntity(credentialsEntity)
				.setNillable(false);
		addAttribute(MAPPER).setNillable(false);
	}
}
