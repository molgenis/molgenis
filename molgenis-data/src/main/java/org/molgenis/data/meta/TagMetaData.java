package org.molgenis.data.meta;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.MolgenisFieldTypes;
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
		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(OBJECT_IRI, ROLE_LOOKUP).setDataType(MolgenisFieldTypes.TEXT);
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setNillable(false);
		addAttribute(RELATION_IRI).setNillable(false);
		addAttribute(RELATION_LABEL).setNillable(false);
		addAttribute(CODE_SYSTEM);
	}
}
