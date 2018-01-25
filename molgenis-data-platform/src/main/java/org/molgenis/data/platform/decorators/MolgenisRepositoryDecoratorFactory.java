package org.molgenis.data.platform.decorators;

import org.molgenis.data.*;
import org.molgenis.data.cache.l1.L1Cache;
import org.molgenis.data.cache.l1.L1CacheRepositoryDecorator;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l2.L2CacheRepositoryDecorator;
import org.molgenis.data.cache.l3.L3Cache;
import org.molgenis.data.cache.l3.L3CacheRepositoryDecorator;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryDecorator;
import org.molgenis.data.index.IndexedRepositoryDecoratorFactory;
import org.molgenis.data.listeners.EntityListenerRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.security.RepositorySecurityDecorator;
import org.molgenis.data.security.aggregation.AggregateAnonymizer;
import org.molgenis.data.security.aggregation.AggregateAnonymizerRepositoryDecorator;
import org.molgenis.data.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionalRepositoryDecorator;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.data.validation.*;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.owned.OwnedEntityType.OWNED;

@Component
public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	private final EntityManager entityManager;
	private final EntityAttributesValidator entityAttributesValidator;
	private final AggregateAnonymizer aggregateAnonymizer;
	private final AppSettings appSettings;
	private final DataService dataService;
	private final SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final IndexActionRegisterService indexActionRegisterService;
	private final IndexedRepositoryDecoratorFactory indexedRepositoryDecoratorFactory;
	private final L1Cache l1Cache;
	private final EntityListenersService entityListenersService;
	private final L2Cache l2Cache;
	private final TransactionInformation transactionInformation;
	private final L3Cache l3Cache;
	private final PlatformTransactionManager transactionManager;
	private final QueryValidator queryValidator;
	private final DefaultValueReferenceValidator defaultValueReferenceValidator;

	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager,
			EntityAttributesValidator entityAttributesValidator, AggregateAnonymizer aggregateAnonymizer,
			AppSettings appSettings, DataService dataService,
			SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry,
			IndexActionRegisterService indexActionRegisterService,
			IndexedRepositoryDecoratorFactory indexedRepositoryDecoratorFactory, L1Cache l1Cache, L2Cache l2Cache,
			TransactionInformation transactionInformation, EntityListenersService entityListenersService,
			L3Cache l3Cache, PlatformTransactionManager transactionManager, QueryValidator queryValidator,
			DefaultValueReferenceValidator defaultValueReferenceValidator)

	{
		this.entityManager = requireNonNull(entityManager);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.aggregateAnonymizer = requireNonNull(aggregateAnonymizer);
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
		this.indexedRepositoryDecoratorFactory = requireNonNull(indexedRepositoryDecoratorFactory);
		this.l1Cache = requireNonNull(l1Cache);
		this.entityListenersService = requireNonNull(entityListenersService);
		this.l2Cache = requireNonNull(l2Cache);
		this.transactionInformation = requireNonNull(transactionInformation);
		this.l3Cache = requireNonNull(l3Cache);
		this.transactionManager = requireNonNull(transactionManager);
		this.queryValidator = requireNonNull(queryValidator);
		this.defaultValueReferenceValidator = requireNonNull(defaultValueReferenceValidator);
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repository;

		// 14. Query the L2 cache before querying the database
		decoratedRepository = new L2CacheRepositoryDecorator(decoratedRepository, l2Cache, transactionInformation);

		// 13. Query the L1 cache before querying the database
		decoratedRepository = new L1CacheRepositoryDecorator(decoratedRepository, l1Cache);

		// 12. Route specific queries to the index
		decoratedRepository = indexedRepositoryDecoratorFactory.create(decoratedRepository);

		// 11. Query the L3 cache before querying the index
		decoratedRepository = new L3CacheRepositoryDecorator(decoratedRepository, l3Cache, transactionInformation);

		// 10. Register the cud action needed to index indexed repositories
		decoratedRepository = new IndexActionRepositoryDecorator(decoratedRepository, indexActionRegisterService);

		// 9. Custom decorators
		decoratedRepository = repositoryDecoratorRegistry.decorate(decoratedRepository);

		// 8. Perform cascading deletes
		decoratedRepository = new CascadeDeleteRepositoryDecorator(decoratedRepository, dataService);

		// 7. Owned decorator
		if (EntityUtils.doesExtend(decoratedRepository.getEntityType(), OWNED))
		{
			decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
		}

		// 6. Entity reference resolver decorator
		decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

		// 5. Entity listener
		decoratedRepository = new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

		// 4. validation decorator
		decoratedRepository = new RepositoryValidationDecorator(dataService, decoratedRepository,
				entityAttributesValidator, defaultValueReferenceValidator);

		// 3. aggregate anonymization decorator
		decoratedRepository = new AggregateAnonymizerRepositoryDecorator<>(decoratedRepository, aggregateAnonymizer,
				appSettings);

		// 2. security decorator
		decoratedRepository = new RepositorySecurityDecorator(decoratedRepository);

		// 1. transaction decorator
		decoratedRepository = new TransactionalRepositoryDecorator<>(decoratedRepository, transactionManager);

		// 0. query validation decorator
		decoratedRepository = new QueryValidationRepositoryDecorator<>(decoratedRepository, queryValidator);

		return decoratedRepository;
	}
}
