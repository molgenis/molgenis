package org.molgenis.data.security.owned;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.EntityIdentity;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.data.security.exception.EntityPermissionDeniedException;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Sid;

import static java.util.Objects.requireNonNull;

/**
 * RepositoryDecorator that works on EntityTypes that are row-level secured.
 * It is reponsible to check that the {@link EntityPermission}s on Entities in this repository are allowed.
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
	public boolean isActionPermitted(Entity entity, Action operation)
	{
		return isActionPermitted(toEntityIdentity(entity), operation);
	}

	@Override
	public boolean isActionPermitted(Object id, Action operation)
	{
		return isActionPermitted(toEntityIdentity(id), operation);
	}

	@Override
	public void throwPermissionException(Entity entity, Action action)
	{
		throw new EntityPermissionDeniedException(getPermission(action), entity);
	}

	private boolean isActionPermitted(EntityIdentity entityIdentity, Action action)
	{
		if (action == Action.CREATE)
		{
			return true;
		}
		EntityPermission entityPermission = getPermission(action);
		return userPermissionEvaluator.hasPermission(entityIdentity, entityPermission);
	}

	/**
	 * Finds out what permission to check for an operation that is being performed on this repository.
	 *
	 * @param operation the Operation that is being performed on the repository
	 * @return the EntityPermission to check
	 */
	private EntityPermission getPermission(Action operation)
	{
		EntityPermission result;
		switch (operation)
		{
			case COUNT:
			case READ:
				result = EntityPermission.READ;
				break;
			case UPDATE:
				result = EntityPermission.UPDATE;
				break;
			case DELETE:
				result = EntityPermission.DELETE;
				break;
			case CREATE:
				throw new UnexpectedEnumException(Action.CREATE);
			default:
				throw new IllegalArgumentException("Illegal operation");
		}
		return result;
	}

	@Override
	public void createAcl(Entity entity)
	{
		MutableAcl acl = mutableAclService.createAcl(new EntityIdentity(entity));
		Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
		acl.insertAce(acl.getEntries().size(), PermissionSet.WRITE, sid, true);
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

	protected Permission toMessagePermission(Action action)
	{
		return getPermission(action);
	}
}
