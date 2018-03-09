package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.meta.system.SystemAttribute;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.BootstrapEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

/**
 * Base class for all system entity meta data.
 */
public abstract class SystemEntityType extends EntityType
{
	private AttributeFactory attributeFactory;
	private IdGenerator idGenerator;

	private final String entityTypeId;
	private boolean rowLevelSecured;

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
		if (!systemPackageName.startsWith(PACKAGE_SYSTEM))
		{
			throw new IllegalArgumentException(
					format("Entity [%s] must be located in package [%s] instead of [%s]", entityName, PACKAGE_SYSTEM,
							systemPackageName));
		}
		this.entityTypeId = systemPackageName + PACKAGE_SEPARATOR + entityName;
	}

	@Override
	public Object get(String attributeName)
	{
		if (attributeName.equals(ID)) return entityTypeId;
		return super.get(attributeName);
	}

	@Override
	public Object getIdValue()
	{
		return entityTypeId;
	}

	@Override
	public String getString(String attributeName)
	{
		if (attributeName.equals(ID)) return entityTypeId;
		return super.getString(attributeName);
	}

	public void setRowLevelSecured(boolean rowLevelSecured)
	{
		this.rowLevelSecured = rowLevelSecured;
	}

	public boolean isRowLevelSecured()
	{
		return this.rowLevelSecured;
	}

	public void bootstrap(EntityTypeMetadata entityTypeMetadata)
	{
		super.init(new BootstrapEntity(entityTypeMetadata));
		setId(entityTypeId);
		setDefaultValues();
		init();
	}

	/**
	 * Initialize system entity meta data, e.g. adding attributes, setting package
	 */
	protected abstract void init();

	@Override
	public String getId()
	{
		return this.entityTypeId;
	}

	public Attribute addAttribute(String attrName, AttributeRole... attrTypes)
	{
		Attribute attr = new SystemAttribute(attributeFactory.getAttributeMetadata());
		attr.setIdentifier(idGenerator.generateId());
		attr.setDefaultValues();
		attr.setName(attrName);
		addAttribute(attr, attrTypes);
		return attr;
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeFactory(AttributeFactory attributeFactory)
	{
		this.attributeFactory = requireNonNull(attributeFactory);
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
