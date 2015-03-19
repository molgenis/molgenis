package org.molgenis.data.meta;

import org.molgenis.data.support.DefaultEntityMetaData;

public class TagMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "tags";
	public static final String IDENTIFIER = "identifier";
	public static final String OBJECT_IRI = "objectIRI";
	public static final String LABEL = "label";
	public static final String RELATION_IRI = "relationIRI";
	public static final String RELATION_LABEL = "relationLabel";
	public static final String CODE_SYSTEM = "codeSystem";

	public TagMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(OBJECT_IRI).setLookupAttribute(true);
		addAttribute(LABEL).setNillable(false).setLookupAttribute(true).setLabelAttribute(true);
		addAttribute(RELATION_IRI).setNillable(false);
		addAttribute(RELATION_LABEL).setNillable(false);
		addAttribute(CODE_SYSTEM);
	}
}
