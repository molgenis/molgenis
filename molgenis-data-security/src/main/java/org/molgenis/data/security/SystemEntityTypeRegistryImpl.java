package org.molgenis.data.security;

import com.google.common.collect.Maps;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.UserPermissionEvaluator;
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
	private static final Logger LOG = LoggerFactory.getLogger(SystemEntityTypeRegistryImpl.class);

	private final Map<String, SystemEntityType> systemEntityTypeMap;
	private final UserPermissionEvaluator permissionService;

	SystemEntityTypeRegistryImpl(UserPermissionEvaluator permissionService)
	{
		this.permissionService = requireNonNull(permissionService);
		systemEntityTypeMap = Maps.newHashMap();
	}

	@Override
	public SystemEntityType getSystemEntityType(String entityTypeId)
	{
		SystemEntityType systemEntityType = systemEntityTypeMap.get(entityTypeId);
		if (systemEntityType != null)
		{
			validateReadPermission(systemEntityType);
		}
		return systemEntityType;
	}

	@Override
	public Stream<SystemEntityType> getSystemEntityTypes()
	{
		return systemEntityTypeMap.values().stream().filter(this::isReadAllowed);
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
		return systemEntityTypeMap.values()
								  .stream()
								  .map(systemEntityType -> getSystemAttribute(systemEntityType, attrIdentifier))
								  .anyMatch(Objects::nonNull);
	}

	@Override
	public Attribute getSystemAttribute(String attrIdentifier)
	{
		return systemEntityTypeMap.values().stream().map(systemEntityType ->
		{
			Attribute attribute = getSystemAttribute(systemEntityType, attrIdentifier);
			if (attribute != null)
			{
				validateReadPermission(systemEntityType);
			}
			return attribute;
		}).filter(Objects::nonNull).findFirst().orElse(null);
	}

	/**
	 * See {@link Permission#COUNT} for an explanation why we are not using {@link Permission#READ} here
	 */
	private boolean isReadAllowed(SystemEntityType systemEntityType)
	{
		return permissionService.hasPermission(new EntityTypeIdentity(systemEntityType.getId()),
				EntityTypePermission.COUNT);
	}

	private void validateReadPermission(SystemEntityType systemEntityType)
	{
		if (!isReadAllowed(systemEntityType))
		{
			throw new MolgenisDataAccessException(
					format("No read permission on entity type '%s' with id '%s'", systemEntityType.getLabel(),
							systemEntityType.getId()));
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
