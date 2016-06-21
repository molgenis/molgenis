package org.molgenis.data.meta.model;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class TagMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "tags";
	public static final String TAG = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String OBJECT_IRI = "objectIRI";
	public static final String LABEL = "label";
	public static final String RELATION_IRI = "relationIRI";
	public static final String RELATION_LABEL = "relationLabel";
	public static final String CODE_SYSTEM = "codeSystem";

	TagMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	@Override
	public void init()
	{
		addAttribute(IDENTIFIER, ROLE_ID).setLabel("Identifier");
		addAttribute(OBJECT_IRI, ROLE_LOOKUP).setDataType(TEXT).setLabel("Object IRI");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Label");
		addAttribute(RELATION_IRI).setNillable(false).setLabel("Relation IRI");
		addAttribute(RELATION_LABEL).setNillable(false).setLabel("Relation label");
		addAttribute(CODE_SYSTEM).setLabel("Code system");
	}
}
