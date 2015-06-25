package org.molgenis.textmining;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class PublicationAnnotationMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "PublicationAnnotation";
	public static final String IDENTIFIER = "identifier";
	public static final String CONTENT = "content";

	public PublicationAnnotationMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(IDENTIFIER).setIdAttribute(true).setAuto(true).setNillable(false).setVisible(false);
		addAttribute(CONTENT).setDataType(MolgenisFieldTypes.HTML).setNillable(false);
	}

}
