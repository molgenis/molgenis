package org.molgenis.data.security.meta;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.acls.model.MutableAclService;

import static java.util.Objects.requireNonNull;

public class EntityTypeRepositorySecurityDecorator extends AbstractRepositoryDecorator<EntityType>
{
	private final MutableAclService mutableAclService;

	public EntityTypeRepositorySecurityDecorator(Repository<EntityType> delegateRepository,
			MutableAclService mutableAclService)
	{
		super(delegateRepository);
		this.mutableAclService = requireNonNull(mutableAclService);
	}
}