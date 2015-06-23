package org.molgenis.textmining;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.file.FileMetaMetaData;
import org.springframework.stereotype.Component;

@Component
public class MutationArticleMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MutationArticle";
	public static final String ID = "id";
	public static final String PDF_FILE = "pdfFile";

	public MutationArticleMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(ID).setIdAttribute(true).setAuto(true).setNillable(false).setVisible(false);
		addAttribute(PDF_FILE).setDataType(MolgenisFieldTypes.FILE).setRefEntity(new FileMetaMetaData())
				.setNillable(false).setLabel("PDF").setDescription("The atricle pdf file.");
	}
}
