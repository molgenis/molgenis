package org.molgenis.data.security;

import com.google.common.collect.Maps;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.COMPOUND;

@Component
public class SystemEntityTypeRegistryImpl implements SystemEntityTypeRegistry
{
	private final Logger LOG = LoggerFactory.getLogger(SystemEntityTypeRegistryImpl.class);

	private final Map<String, SystemEntityType> systemEntityTypeMap;
	private final PermissionService permissionService;

	public SystemEntityTypeRegistryImpl(PermissionService permissionService)
	{
		this.permissionService = requireNonNull(permissionService);
		systemEntityTypeMap = Maps.newHashMap();
	}

	@Override
	public SystemEntityType getSystemEntityType(String entityTypeId)
	{
		SystemEntityType systemEntityType = systemEntityTypeMap.get(entityTypeId);
		validateReadPermission(systemEntityType);
		return systemEntityType;
	}

	@Override
	public Stream<SystemEntityType> getSystemEntityTypes()
	{
		return systemEntityTypeMap.values().stream().filter(systemEntityType ->
		{
			validateReadPermission(systemEntityType);
			return true;
		});
	}

	@Override
	public boolean hasSystemEntityType(String entityTypeId)
	{
		return systemEntityTypeMap.containsKey(entityTypeId);
	}

	@Override
	public void addSystemEntityType(SystemEntityType systemEntityType)
	{
		String systemEntityTypeName = systemEntityType.getId();
		LOG.trace("Registering system entity [{}] ...", systemEntityTypeName);
		systemEntityTypeMap.put(systemEntityTypeName, systemEntityType);
	}

	@Override
	public boolean hasSystemAttribute(String attrIdentifier)
	{
		return getSystemAttribute(attrIdentifier) != null;
	}

	@Override
	public Attribute getSystemAttribute(String attrIdentifier)
	{
		return getSystemEntityTypes().map(systemEntityType -> getSystemAttribute(systemEntityType, attrIdentifier))
									 .filter(Objects::nonNull)
									 .findFirst()
									 .orElse(null);
	}

	private void validateReadPermission(SystemEntityType systemEntityType)
	{
		String systemEntityTypeId = systemEntityType.getId();
		if (!permissionService.hasPermissionOnEntityType(systemEntityTypeId, Permission.READ))
		{
			throw new MolgenisDataAccessException(format("No read permission on entity type '%s'", systemEntityTypeId));
		}
	}

	private static Attribute getSystemAttribute(SystemEntityType systemEntityType, String attrIdentifier)
	{
		return getSystemAttributeRec(systemEntityType.getAllAttributes(), attrIdentifier);
	}

	private static Attribute getSystemAttributeRec(Iterable<Attribute> attrs, String attrIdentifier)
	{
		for (Attribute attr : attrs)
		{
			if (attr.getIdentifier() != null && attr.getIdentifier().equals(attrIdentifier))
			{
				return attr;
			}
			else
			{
				if (attr.getDataType() == COMPOUND)
				{
					return getSystemAttributeRec(attr.getChildren(), attrIdentifier);
				}
			}
		}
		return null;
	}
}
