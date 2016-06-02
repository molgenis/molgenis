package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntityMetaDataMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME_ = "entities";
	public static final String ENTITY_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME_;

	public static final String SIMPLE_NAME = "simpleName";
	public static final String BACKEND = "backend";
	public static final String FULL_NAME = "fullName";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String LABEL_ATTRIBUTE = "labelAttribute";
	public static final String LOOKUP_ATTRIBUTES = "lookupAttributes";
	public static final String ABSTRACT = "abstract";
	public static final String LABEL = "label";
	public static final String EXTENDS = "extends";
	public static final String DESCRIPTION = "description";
	public static final String PACKAGE = "package";
	public static final String TAGS = "tags";
	public static final String ATTRIBUTES = "attributes";

	private AttributeMetaDataMetaData attributeMetaDataMetaData;
	private PackageMetaData packageMetaData;
	private TagMetaData tagMetaData;

	EntityMetaDataMetaData()
	{
		super(SIMPLE_NAME_, PACKAGE_META);
	}

	public void init()
	{
		addAttribute(FULL_NAME, ROLE_ID).setUnique(true);
		addAttribute(SIMPLE_NAME, ROLE_LABEL).setNillable(false);
		addAttribute(BACKEND);
		addAttribute(PACKAGE).setDataType(XREF).setRefEntity(packageMetaData);
		addAttribute(ID_ATTRIBUTE).setDataType(XREF).setRefEntity(attributeMetaDataMetaData);
		addAttribute(LABEL_ATTRIBUTE).setDataType(XREF).setRefEntity(attributeMetaDataMetaData);
		addAttribute(LOOKUP_ATTRIBUTES).setDataType(MREF).setRefEntity(attributeMetaDataMetaData);
		addAttribute(ABSTRACT).setDataType(BOOL);
		addAttribute(LABEL, ROLE_LOOKUP);
		addAttribute(EXTENDS).setDataType(XREF)
				.setRefEntity(this); // TODO replace with autowired self-reference after update to Spring 4.3
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetaData);
		addAttribute(ATTRIBUTES).setDataType(MREF).setRefEntity(attributeMetaDataMetaData);

		addAttribute(LABEL + '-' + "en").setNillable(true);
		addAttribute(DESCRIPTION + '-' + "en").setNillable(true);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataMetaData(AttributeMetaDataMetaData attributeMetaDataMetaData)
	{
		this.attributeMetaDataMetaData = requireNonNull(attributeMetaDataMetaData);
	}

	@Autowired
	public void setPackageMetaData(PackageMetaData packageMetaData)
	{
		this.packageMetaData = requireNonNull(packageMetaData);
	}

	@Autowired
	public void setTagMetaData(TagMetaData tagMetaData)
	{
		this.tagMetaData = requireNonNull(tagMetaData);
	}
}
