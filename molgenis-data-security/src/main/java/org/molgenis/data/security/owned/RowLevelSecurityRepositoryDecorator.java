package org.molgenis.data.security.owned;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.EntityPermissionUtils;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.security.acls.domain.AbstractPermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.EntityPermission.WRITE;
import static org.molgenis.data.security.owned.AbstractRowLevelSecurityRepositoryDecorator.Action.CREATE;

/**
 * RepositoryDecorator that works on EntityTypes that are row-level secured.
 */
public class RowLevelSecurityRepositoryDecorator extends AbstractRowLevelSecurityRepositoryDecorator<Entity>
{
	private final UserPermissionEvaluator userPermissionEvaluator;
	private final MutableAclService mutableAclService;

	RowLevelSecurityRepositoryDecorator(Repository delegateRepository,
			UserPermissionEvaluator userPermissionEvaluator, MutableAclService mutableAclService)
	{
		super(delegateRepository, mutableAclService);
		this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public boolean isOperationPermitted(Entity entity, Action action)
	{
		return isOperationPermitted(toEntityIdentity(entity), action);
	}

	@Override
	public boolean isOperationPermitted(Object id, Action action)
	{
		return isOperationPermitted(toEntityIdentity(id), action);
	}

	private boolean isOperationPermitted(EntityIdentity entityIdentity, Action action)
	{
		AbstractPermission permission = getPermissionForOperation(action);
		return userPermissionEvaluator.hasPermission(entityIdentity, permission);
	}

	private EntityPermission getPermissionForOperation(Action action)
	{
		EntityPermission permission;
		switch (action)
		{
			case COUNT:
				permission = EntityPermission.COUNT;
				break;
			case READ:
				permission = EntityPermission.READ;
				break;
			case UPDATE:
			case DELETE:
				permission = EntityPermission.WRITE;
				break;
			case CREATE:
				throw new UnexpectedEnumException(CREATE);
			default:
				throw new IllegalArgumentException("Illegal entity type permission");
		}
		return permission;
	}

	@Override
	public void createAcl(Entity entity)
	{
		MutableAcl acl = mutableAclService.createAcl(new EntityIdentity(entity));
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
		acl.insertAce(acl.getEntries().size(), EntityPermissionUtils.getCumulativePermission(WRITE), sid, true);
		mutableAclService.updateAcl(acl);
	}

	@Override
	public void deleteAcl(Entity entity)
	{
		EntityIdentity entityIdentity = new EntityIdentity(entity);
		deleteAcl(entityIdentity);
	}

	@Override
	public void deleteAcl(Object id)
	{
		EntityIdentity entityIdentity = toEntityIdentity(id);
		deleteAcl(entityIdentity);
	}

	@Override
	public void updateAcl(Entity entity)
	{
		//No action required
	}

	private EntityIdentity toEntityIdentity(Object entityId)
	{
		return new EntityIdentity(getEntityType().getId(), entityId);
	}

	private EntityIdentity toEntityIdentity(Entity entity)
	{
		return new EntityIdentity(entity.getEntityType().getId(), entity.getIdValue());
	}

	protected String toMessagePermission(Action action)
	{
		return getPermissionForOperation(action).getName();
	}
}
