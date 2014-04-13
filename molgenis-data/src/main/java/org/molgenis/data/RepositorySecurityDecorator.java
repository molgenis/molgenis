package org.molgenis.data;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.security.core.Permission;

public class RepositorySecurityDecorator extends RepositoryDecorator implements Repository
{
	private final Repository decoratedRepository;
	private final String roleEntityName;

	public RepositorySecurityDecorator(Repository decoratedRepository)
	{
		super(decoratedRepository);
		if (decoratedRepository == null) throw new IllegalArgumentException("decoratedRepsitory is null");
		this.decoratedRepository = decoratedRepository;
		this.roleEntityName = decoratedRepository.getName().toUpperCase();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		validatePermission(Permission.READ);
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.close();
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.iterator(clazz);
	}

	@Override
	public String getUrl()
	{
		return decoratedRepository.getUrl();
	}

	protected void validatePermission(Permission permission)
	{
		String role = String.format("ROLE_ENTITY_%s_%s", roleEntityName, permission.toString());
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", role))
		{
			throw new MolgenisDataAccessException("No read permission on " + roleEntityName);
		}
	}
}
