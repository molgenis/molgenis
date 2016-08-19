package org.molgenis.data.meta.model;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.Boolean.FALSE;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class EntityMetaDataMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME_ = "entities";
	public static final String ENTITY_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME_;

	public static final String FULL_NAME = "fullName";
	public static final String SIMPLE_NAME = "simpleName";
	public static final String PACKAGE = "package";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String ATTRIBUTES = "attributes";
	public static final String ID_ATTRIBUTE = "idAttribute";
	public static final String LABEL_ATTRIBUTE = "labelAttribute";
	public static final String LOOKUP_ATTRIBUTES = "lookupAttributes";
	public static final String ABSTRACT = "abstract";
	public static final String EXTENDS = "extends";
	public static final String TAGS = "tags";
	public static final String BACKEND = "backend";

	private AttributeMetaDataMetaData attrMetaMeta;
	private PackageMetaData packageMetaData;
	private TagMetaData tagMetaData;

	EntityMetaDataMetaData()
	{
		super(SIMPLE_NAME_, PACKAGE_META);
	}

	public void init()
	{
		setLabel("Entity");
		setDescription("Meta data for entity classes");

		addAttribute(FULL_NAME, ROLE_ID).setVisible(false).setLabel("Qualified name");
		addAttribute(SIMPLE_NAME, ROLE_LABEL).setNillable(false).setReadOnly(true).setLabel("Name");
		// TODO discuss whether package should be nillable
		addAttribute(PACKAGE).setDataType(XREF).setRefEntity(packageMetaData).setLabel("Package").setReadOnly(true);
		addAttribute(LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(ATTRIBUTES).setDataType(MREF).setRefEntity(attrMetaMeta).setNillable(false).setLabel("Attributes");
		addAttribute(ID_ATTRIBUTE).setDataType(XREF).setRefEntity(attrMetaMeta).setReadOnly(true)
				.setLabel("ID attribute");
		addAttribute(LABEL_ATTRIBUTE).setDataType(XREF).setRefEntity(attrMetaMeta).setLabel("Label attribute");
		addAttribute(LOOKUP_ATTRIBUTES).setDataType(MREF).setRefEntity(attrMetaMeta).setLabel("Lookup attributes");
		addAttribute(ABSTRACT).setDataType(BOOL).setNillable(false).setReadOnly(true).setLabel("Abstract")
				.setDefaultValue(FALSE.toString());
		// TODO replace with autowired self-reference after update to Spring 4.3
		addAttribute(EXTENDS).setDataType(XREF).setRefEntity(this).setReadOnly(true).setLabel("Extends");
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetaData).setLabel("Tags");
		addAttribute(BACKEND).setNillable(false).setReadOnly(true).setLabel("Backend")
				.setDescription("Backend data store");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataMetaData(AttributeMetaDataMetaData attrMetaMeta)
	{
		this.attrMetaMeta = requireNonNull(attrMetaMeta);
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
