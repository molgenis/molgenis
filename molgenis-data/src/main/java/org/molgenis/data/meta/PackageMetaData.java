package org.molgenis.data.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PackageMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME_ = "packages";
	public static final String PACKAGE = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME_;

	public static final String FULL_NAME = "fullName";
	public static final String SIMPLE_NAME = "name";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String TAGS = "tags";

	private TagMetaData tagMetaData;

	PackageMetaData()
	{
		super(SIMPLE_NAME_, PACKAGE_META);
	}

	@Override
	public void init()
	{
		addAttribute(FULL_NAME, ROLE_ID, ROLE_LABEL).setNillable(false);
		addAttribute(SIMPLE_NAME);
		addAttribute(LABEL);
		addAttribute(DESCRIPTION).setDataType(TEXT);
		addAttribute(PARENT).setDataType(XREF).setRefEntity(this);
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetaData);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setTagMetaData(TagMetaData tagMetaData)
	{
		this.tagMetaData = requireNonNull(tagMetaData);
	}
}
