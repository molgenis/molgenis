package org.molgenis.data.meta;

import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.system.SystemAttributeMetaData;
import org.molgenis.data.support.BootstrapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * Base class for all system entity meta data.
 */
public abstract class SystemEntityType extends EntityMetaData
{
	private AttributeMetaDataFactory attributeMetaDataFactory;
	private IdGenerator idGenerator;

	private final String entityName;
	private final String systemPackageName;

	/**
	 * Construct entity meta data for an entity with the given name stored in the system package.
	 *
	 * @param entityName entity name
	 */
	public SystemEntityType(String entityName)
	{
		this(entityName, PACKAGE_SYSTEM);
	}

	/**
	 * Construct entity meta data for an entity with the given name stored in a system package with the given package name.
	 *
	 * @param entityName        entity name
	 * @param systemPackageName system package name
	 */
	public SystemEntityType(String entityName, String systemPackageName)
	{
		this.entityName = requireNonNull(entityName);
		this.systemPackageName = requireNonNull(systemPackageName);
		if (!systemPackageName.startsWith(PACKAGE_SYSTEM))
		{
			throw new IllegalArgumentException(
					format("Entity [%s] must be located in package [%s] instead of [%s]", entityName, PACKAGE_SYSTEM,
							systemPackageName));
		}
	}

	public void bootstrap(EntityTypeMetadata entityTypeMetadata)
	{
		super.init(new BootstrapEntity(entityTypeMetadata));
		setName(systemPackageName + PACKAGE_SEPARATOR + entityName);
		setSimpleName(entityName);
		setDefaultValues();
		init();
	}

	/**
	 * Initialize system entity meta data, e.g. adding attributes, setting package
	 */
	protected abstract void init();

	@Override
	public String getName()
	{
		return systemPackageName + PACKAGE_SEPARATOR + entityName;
	}

	@Override
	public String getSimpleName()
	{
		return entityName;
	}

	public AttributeMetaData addAttribute(String attrName, AttributeRole... attrTypes)
	{
		return addAttribute(attrName, null, attrTypes);
	}

	public AttributeMetaData addAttribute(String attrName, AttributeMetaData parentAttr, AttributeRole... attrTypes)
	{
		AttributeMetaData attr = new SystemAttributeMetaData(attributeMetaDataFactory.getAttributeMetaDataMetaData());
		attr.setIdentifier(idGenerator.generateId());
		attr.setDefaultValues();
		attr.setName(attrName);
		if (parentAttr != null)
		{
			parentAttr.addAttributePart(attr);
			setAttributeRoles(attr, attrTypes);
		}
		else
		{
			addAttribute(attr, attrTypes);
		}
		return attr;
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMetaDataFactory(AttributeMetaDataFactory attributeMetaDataFactory)
	{
		this.attributeMetaDataFactory = requireNonNull(attributeMetaDataFactory);
	}

	@Autowired
	public void setIdGenerator(IdGenerator idGenerator)
	{
		this.idGenerator = requireNonNull(idGenerator);
	}

	/**
	 * Used to determine the order of bootstrapping.
	 *
	 * @return Set containing the {@link SystemEntityType}s that this {@link SystemEntityType}'s definition depends upon.
	 */
	public Set<SystemEntityType> getDependencies()
	{
		return Collections.emptySet();
	}
}
