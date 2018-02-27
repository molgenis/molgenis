package org.molgenis.data.validation.meta.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.security.meta.RowLevelSecuredMetadata;
import org.molgenis.data.security.meta.RowLevelSecurityConfiguration;
import org.molgenis.data.security.owned.RowLevelSecurityConfigurationEntityDecorator;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * Due to a circular dependency this decorator factory is not stored in molgenis-data.
 */
@Component
public class RowLevelSecurityRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<RowLevelSecurityConfiguration, RowLevelSecuredMetadata>
{
	private final DataService dataService;
	private final MutableAclService mutableAclService;

	public RowLevelSecurityRepositoryDecoratorFactory(DataService dataService,
			RowLevelSecuredMetadata entityTypeMetadata, MutableAclService mutableAclService)
	{
		super(entityTypeMetadata);
		this.dataService = requireNonNull(dataService);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public Repository<RowLevelSecurityConfiguration> createDecoratedRepository(
			Repository<RowLevelSecurityConfiguration> repository)
	{
		return new RowLevelSecurityConfigurationEntityDecorator(repository, mutableAclService, dataService);
	}
}
