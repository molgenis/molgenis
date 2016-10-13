package org.molgenis.data.meta.model;

import org.molgenis.data.Sort;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.lang.Boolean.FALSE;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.SEQUENCE_NR;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class EntityTypeMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME_ = "EntityType";
	public static final String ENTITY_TYPE_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME_;

	public static final String FULL_NAME = "fullName";
	public static final String SIMPLE_NAME = "simpleName";
	public static final String PACKAGE = "package";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String ATTRIBUTES = "attributes";
	public static final String IS_ABSTRACT = "isAbstract";
	public static final String EXTENDS = "extends";
	public static final String TAGS = "tags";
	public static final String BACKEND = "backend";

	private AttributeMetadata attributeMetadata;
	private PackageMetadata packageMetadata;
	private TagMetaData tagMetaData;

	EntityTypeMetadata()
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
		addAttribute(PACKAGE).setDataType(XREF).setRefEntity(packageMetadata).setLabel("Package").setReadOnly(true);
		addAttribute(LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		Attribute refAttr = attributeMetadata.getAttribute(AttributeMetadata.ENTITY);
		addAttribute(ATTRIBUTES).setDataType(ONE_TO_MANY).setRefEntity(attributeMetadata).setMappedBy(refAttr)
				.setOrderBy(new Sort(SEQUENCE_NR)).setNillable(true).setLabel("Attributes");
		addAttribute(IS_ABSTRACT).setDataType(BOOL).setNillable(false).setReadOnly(true).setLabel("Abstract")
				.setReadOnly(true).setDefaultValue(FALSE.toString());
		// TODO replace with autowired self-reference after update to Spring 4.3
		addAttribute(EXTENDS).setDataType(XREF).setRefEntity(this).setReadOnly(true).setLabel("Extends");
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetaData).setLabel("Tags");
		addAttribute(BACKEND).setNillable(false).setReadOnly(true).setLabel("Backend")
				.setDescription("Backend data store");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetadata(AttributeMetadata attributeMetadata)
	{
		this.attributeMetadata = requireNonNull(attributeMetadata);
	}

	@Autowired
	public void setPackageMetadata(PackageMetadata packageMetadata)
	{
		this.packageMetadata = requireNonNull(packageMetadata);
	}

	@Autowired
	public void setTagMetaData(TagMetaData tagMetaData)
	{
		this.tagMetaData = requireNonNull(tagMetaData);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return singleton(attributeMetadata);
	}
}
