package org.molgenis.data.meta.model;

import org.molgenis.data.Sort;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.lang.Boolean.FALSE;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.SEQUENCE_NR;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.MetaPackage.PACKAGE_META;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class EntityTypeMetadata extends SystemEntityType
{
	private static final String SIMPLE_NAME = "EntityType";
	public static final String ENTITY_TYPE_META_DATA = PACKAGE_META + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private AttributeMetadata attributeMetadata;
	private PackageMetadata packageMetadata;
	private TagMetadata tagMetadata;

	public static final String ID = "id";
	public static final String PACKAGE = "package";
	public static final String LABEL = "label";
	public static final String DESCRIPTION = "description";
	public static final String ATTRIBUTES = "attributes";
	public static final String IS_ABSTRACT = "isAbstract";
	public static final String EXTENDS = "extends";
	public static final String TAGS = "tags";
	public static final String BACKEND = "backend";
	public static final String INDEXING_DEPTH = "indexingDepth";

	private List<String> backendEnumOptions;
	private String defaultBackend;

	EntityTypeMetadata()
	{
		super(SIMPLE_NAME, PACKAGE_META);
	}

	public void init()
	{
		requireNonNull(backendEnumOptions, "backend enum options not set!");

		setId(ENTITY_TYPE_META_DATA);
		setLabel("Entity type");
		setDescription("Meta data for entity classes");

		addAttribute(ID, ROLE_ID).setAuto(true).setLabel("Identifier");
		addAttribute(LABEL, ROLE_LABEL, ROLE_LOOKUP).setNillable(false).setLabel("Label");
		addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description");
		addAttribute(PACKAGE).setDataType(XREF).setRefEntity(packageMetadata).setLabel("Package");
		Attribute refAttr = attributeMetadata.getAttribute(AttributeMetadata.ENTITY);
		addAttribute(ATTRIBUTES).setDataType(ONE_TO_MANY)
								.setRefEntity(attributeMetadata)
								.setMappedBy(refAttr)
								.setOrderBy(new Sort(SEQUENCE_NR))
								.setNillable(true)
								.setLabel("Attributes");
		addAttribute(IS_ABSTRACT).setDataType(BOOL)
								 .setNillable(false)
								 .setReadOnly(true)
								 .setLabel("Abstract")
								 .setReadOnly(true)
								 .setDefaultValue(FALSE.toString());
		// TODO replace with autowired self-reference after update to Spring 4.3
		addAttribute(EXTENDS).setDataType(XREF).setRefEntity(this).setReadOnly(true).setLabel("Extends");
		addAttribute(TAGS).setDataType(MREF).setRefEntity(tagMetadata).setLabel("Tags");
		addAttribute(BACKEND).setDataType(ENUM)
							 .setEnumOptions(backendEnumOptions)
							 .setNillable(false)
							 .setReadOnly(true)
							 .setDefaultValue(defaultBackend)
							 .setLabel("Backend")
							 .setDescription("Backend data store");
		addAttribute(INDEXING_DEPTH).setDataType(INT)
									.setLabel("Indexing depth")
									.setDescription(
											"1 = index attributes and referenced entities (default) 2 = index attributes, referenced entities and entities referenced by referenced entities")
									.setNillable(false)
									.setDefaultValue(String.valueOf(1))
									.setRangeMin(1L);
	}

	/**
	 * Used during bootstrapping to set the enum options for the backend field. Circumvents unresolvable circular
	 * dependencies when autowiring RepositoryCollectionRegistry into this bean.
	 *
	 * @param repositoryCollectionNames list of RepositoryCollection names
	 */
	public void setBackendEnumOptions(List<String> repositoryCollectionNames)
	{
		this.backendEnumOptions = requireNonNull(repositoryCollectionNames);
	}

	/**
	 * Used during bootstrapping to set the default value of the backend field.
	 *
	 * @param repositoryCollectionName list of RepositoryCollection names
	 */
	public void setDefaultBackend(String repositoryCollectionName)
	{
		this.defaultBackend = requireNonNull(repositoryCollectionName);
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
	public void setTagMetadata(TagMetadata tagMetadata)
	{
		this.tagMetadata = requireNonNull(tagMetadata);
	}

	@Override
	public Set<SystemEntityType> getDependencies()
	{
		return singleton(attributeMetadata);
	}
}
