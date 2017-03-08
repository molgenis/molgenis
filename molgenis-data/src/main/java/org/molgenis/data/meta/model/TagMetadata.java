package org.molgenis.data.meta.model;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class TagMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "Tag";
	public static final String TAG = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String OBJECT_IRI = "objectIRI";
	public static final String LABEL = "label";
	public static final String RELATION_IRI = "relationIRI";
	public static final String RELATION_LABEL = "relationLabel";
	public static final String CODE_SYSTEM = "codeSystem";

	TagMetadata()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	@Override
	public void init()
	{
		setId(TAG);
		setLabel("Tag");
		setDescription("Semantic tags that can be applied to entities, attributes and other data");

		addAttribute(ID, ROLE_ID).setLabel("Identifier");
		addAttribute(OBJECT_IRI, ROLE_LOOKUP).setDataType(TEXT).setLabel("Object IRI");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Label");
		addAttribute(RELATION_IRI).setNillable(false).setLabel("Relation IRI");
		addAttribute(RELATION_LABEL).setNillable(false).setLabel("Relation label");
		addAttribute(CODE_SYSTEM).setLabel("Code system");
	}
}
