package org.molgenis.ui;

import org.molgenis.auth.MolgenisUserDecorator;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.AutoValueRepositoryDecorator;
import org.molgenis.data.ComputedEntityValuesDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityReferenceResolverDecorator;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositorySecurityDecorator;
import org.molgenis.data.elasticsearch.reindex.ReindexActionRegisterService;
import org.molgenis.data.elasticsearch.reindex.ReindexActionRepositoryDecorator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;

public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	private final EntityManager entityManager;
	private final ReindexActionRegisterService indexTransactionLogService;
	private final EntityAttributesValidator entityAttributesValidator;
	private final IdGenerator idGenerator;
	private final AppSettings appSettings;
	private final DataService dataService;
	private final ExpressionValidator expressionValidator;
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager,
			EntityAttributesValidator entityAttributesValidator, IdGenerator idGenerator, AppSettings appSettings,
			DataService dataService, ExpressionValidator expressionValidator,
			RepositoryDecoratorRegistry repositoryDecoratorRegistry,
			ReindexActionRegisterService indexTransactionLogService)
	{
		this.entityManager = entityManager;
		this.entityAttributesValidator = entityAttributesValidator;
		this.idGenerator = idGenerator;
		this.appSettings = appSettings;
		this.dataService = dataService;
		this.expressionValidator = expressionValidator;
		this.repositoryDecoratorRegistry = repositoryDecoratorRegistry;
		this.indexTransactionLogService = indexTransactionLogService;
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repositoryDecoratorRegistry.decorate(repository);

		if (decoratedRepository.getName().equals(MolgenisUserMetaData.ENTITY_NAME))
		{
			decoratedRepository = new MolgenisUserDecorator(decoratedRepository);
		}

		// 8. Owned decorator
		if (EntityUtils.doesExtend(decoratedRepository.getEntityMetaData(), OwnedEntityMetaData.ENTITY_NAME))
		{
			decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
		}

		// 7. Entity reference resolver decorator
		decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

		// 6. Computed entity values decorator
		decoratedRepository = new ComputedEntityValuesDecorator(decoratedRepository);

		// 5. Entity listener
		decoratedRepository = new EntityListenerRepositoryDecorator(decoratedRepository);

		// 4. Index Transaction log decorator
		decoratedRepository = new ReindexActionRepositoryDecorator(decoratedRepository,
				indexTransactionLogService);

		// 3. validation decorator
		decoratedRepository = new RepositoryValidationDecorator(dataService, decoratedRepository,
				entityAttributesValidator, expressionValidator);

		// 2. auto value decorator
		decoratedRepository = new AutoValueRepositoryDecorator(decoratedRepository, idGenerator);

		// 1. security decorator
		decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, appSettings);

		return decoratedRepository;
	}
}
