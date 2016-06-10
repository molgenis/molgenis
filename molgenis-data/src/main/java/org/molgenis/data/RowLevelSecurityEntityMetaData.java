package org.molgenis.data;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.ROW_LEVEL_SECURITY_ATTRIBUTES;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.isCurrentUserSuOrSystem;

public class RowLevelSecurityEntityMetaData extends DefaultEntityMetaData implements EntityMetaData
{
	public RowLevelSecurityEntityMetaData(EntityMetaData entityMetaData)
	{
		super(entityMetaData);
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return filterPermissionAttributes(super.getAttributes());
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAttributes()
	{
		return filterPermissionAttributes(super.getOwnAttributes());
	}

	@Override
	public Iterable<AttributeMetaData> getAtomicAttributes()
	{
		return filterPermissionAttributes(super.getAtomicAttributes());
	}

	@Override
	public Iterable<AttributeMetaData> getOwnAtomicAttributes()
	{
		return filterPermissionAttributes(super.getOwnAtomicAttributes());
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		AttributeMetaData attr = super.getAttribute(attributeName);
		return attr == null ? null : filterPermissionAttribute(attr);
	}

	private List<AttributeMetaData> filterPermissionAttributes(Iterable<AttributeMetaData> attributes)
	{
		return StreamSupport.stream(attributes.spliterator(), false)
				.filter(attr -> !ROW_LEVEL_SECURITY_ATTRIBUTES.contains(attr.getName()) || SecurityUtils
						.currentUserIsSu() || SecurityUtils.currentUserHasRole(SystemSecurityToken.ROLE_SYSTEM))
				.collect(Collectors.toList());
	}

	private AttributeMetaData filterPermissionAttribute(AttributeMetaData amd)
	{
		if (!ROW_LEVEL_SECURITY_ATTRIBUTES.contains(amd.getName()) || isCurrentUserSuOrSystem()) return amd;
		return null;
	}
}