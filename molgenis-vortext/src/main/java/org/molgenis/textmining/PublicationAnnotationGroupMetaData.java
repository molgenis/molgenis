package org.molgenis.textmining;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

/**
 * Corresponds to a 'org.molgenis.vortext.Marginalis'
 */
@Component
public class PublicationAnnotationGroupMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "PublicationAnnotationGroup";
	public static final String IDENTIFIER = "identifier";
	public static final String TITLE = "title";
	public static final String TYPE = "type";
	public static final String DESCRIPTION = "description";
	public static final String ANNOTATIONS = "annotations";

	public PublicationAnnotationGroupMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(IDENTIFIER).setIdAttribute(true).setAuto(true).setNillable(false).setVisible(false);
		addAttribute(TYPE).setNillable(false).setLabelAttribute(true);
		addAttribute(TITLE).setDataType(MolgenisFieldTypes.HTML);
		addAttribute(DESCRIPTION).setDataType(MolgenisFieldTypes.HTML);
		addAttribute(ANNOTATIONS).setDataType(MolgenisFieldTypes.MREF)
				.setRefEntity(new PublicationAnnotationMetaData());
	}

}
