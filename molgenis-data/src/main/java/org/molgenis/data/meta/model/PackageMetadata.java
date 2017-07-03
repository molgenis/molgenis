package org.molgenis.data.meta.model;

import org.molgenis.data.Sort;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class PackageMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME_ = "Package";
	public static final String PACKAGE = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME_;

	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String PARENT = "parent";
	public static final String CHILDREN = "children";
	public static final String ENTITY_TYPES = "entityTypes";
	public static final String TAGS = "tags";

	private TagMetadata tagMetadata;
	private EntityTypeMetadata entityTypeMetadata;

	public PackageMetadata()
	{
		super(SIMPLE_NAME_, PACKAGE_META);
	}

	@Override
	public void init()
	{
		setId(PACKAGE);
		setLabel("Package");
		setDescription("Grouping of related entities");

		setIndexingDepth(2);

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		Attribute parentAttr = addAttribute(PARENT).setDataType(XREF).setRefEntity(this).setLabel("Parent");
		addAttribute(CHILDREN).setReadOnly(true)
							  .setDataType(ONE_TO_MANY)
							  .setMappedBy(parentAttr)
							  .setOrderBy(new Sort(LABEL))
							  .setRefEntity(this)
							  .setLabel("Children");
		addAttribute(ENTITY_TYPES).setReadOnly(true)
								  .setDataType(ONE_TO_MANY)
								  .setMappedBy(entityTypeMetadata.getAttribute(EntityTypeMetadata.PACKAGE))
								  .setOrderBy(new Sort(EntityTypeMetadata.LABEL))
								  .setOrderBy(new Sort(LABEL))
								  .setRefEntity(entityTypeMetadata)
								  .setLabel("Entity types");
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetadata).setLabel("Tags");
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setTagMetadata(TagMetadata tagMetadata)
	{
		this.tagMetadata = requireNonNull(tagMetadata);
	}

	@Autowired
	public void setEntityTypeMetadata(EntityTypeMetadata entityTypeMetadata)
	{
		this.entityTypeMetadata = requireNonNull(entityTypeMetadata);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return Collections.singleton(entityTypeMetadata);
	}
}
