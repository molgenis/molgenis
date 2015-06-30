package org.molgenis.textmining;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.file.FileMetaMetaData;
import org.molgenis.security.owned.OwnedEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class PublicationMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "Publication";
	public static final String ID = "id";
	public static final String PDF_FILE = "pdfFile";
	public static final String ANNOTATION_GROUPS = "annotationGroups";

	public PublicationMetaData()
	{
		super(ENTITY_NAME);
		this.setExtends(new OwnedEntityMetaData());// FileMeta extends from Owned, so to work properly Publication
													// should also extend from Owned
		addAttribute(ID).setIdAttribute(true).setAuto(true).setNillable(false).setVisible(false);
		addAttribute(PDF_FILE).setDataType(MolgenisFieldTypes.FILE).setRefEntity(new FileMetaMetaData())
				.setNillable(false).setLabel("PDF").setDescription("The atricle pdf file.");
		addAttribute(ANNOTATION_GROUPS).setDataType(MolgenisFieldTypes.MREF).setRefEntity(
				new PublicationAnnotationGroupMetaData());
	}
}
