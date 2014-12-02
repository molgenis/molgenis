package org.molgenis.vkgl;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class ImportedFilesEntityMetaData extends DefaultEntityMetaData
{
	public static final ImportedFilesEntityMetaData INSTANCE = new ImportedFilesEntityMetaData();

	public static final String FILENAME = "filename";
	public static final String IMPORT_DATE = "importdate";
	public static final String ERROR_MESSAGE = "errormessage";

	private static final String ENTITY_NAME = "VkglImportedFiles";

	public ImportedFilesEntityMetaData()
	{
		super(ENTITY_NAME, new PackageImpl(VkglEntityMetaData.PACKAGE_NAME, "VKGL package"));

		addAttributeMetaData(new DefaultAttributeMetaData(FILENAME, MolgenisFieldTypes.FieldTypeEnum.STRING)
				.setIdAttribute(true).setNillable(false));

		addAttributeMetaData(new DefaultAttributeMetaData(IMPORT_DATE).setDataType(MolgenisFieldTypes.DATETIME)
				.setNillable(false));

		addAttributeMetaData(new DefaultAttributeMetaData(ERROR_MESSAGE).setDataType(MolgenisFieldTypes.TEXT));
	}

}
