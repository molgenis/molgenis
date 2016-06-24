package org.molgenis.app.promise.model;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.app.promise.mapper.PromiseMapperFactory;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class PromiseMappingProjectMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "PromiseMappingProject";
	public static final String FULLY_QUALIFIED_NAME = PromisePackage.NAME + '_' + ENTITY_NAME;

	public static final String NAME = "name";
	public static final String BIOBANK_ID = "biobank_id";
	public static final String CREDENTIALS = "Credentials";
	public static final String MAPPER = "mapper";

	public static final String LBL_NAME = "Name";
	public static final String LBL_BIOBANK_ID = "Biobank ID";
	public static final String LBL_CREDENTIALS = "Credentials";
	public static final String LBL_MAPPER = "Mapper";

	@Autowired
	public PromiseMappingProjectMetaData(PromiseMapperFactory promiseMapperFactory)
	{
		super(ENTITY_NAME, PromisePackage.getPackage());

		requireNonNull(promiseMapperFactory);

		setLabel("ProMISe mapping projects");
		setDescription("");

		AttributeMetaData idAttribute = addAttribute(NAME).setNillable(false).setLabel(LBL_NAME)
				.setDescription("The name of this mapping");
		setIdAttribute(idAttribute);

		addAttribute(BIOBANK_ID).setNillable(false).setLabel(LBL_BIOBANK_ID)
				.setDescription("The ID of the biobank in the BBMRI-NL Sample Collections entity").setUnique(true);
		addAttribute(CREDENTIALS).setDataType(MolgenisFieldTypes.XREF).setRefEntity(PromiseCredentialsMetaData.INSTANCE)
				.setNillable(false).setLabel(LBL_CREDENTIALS)
				.setDescription("The ProMISe credentials for this biobank");
		addAttribute(MAPPER).setNillable(false).setLabel(LBL_MAPPER)
				.setDescription("The mapper to use for this biobank");
	}
}
