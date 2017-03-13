package org.molgenis.data.platform.decorators;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateAnonymizer;
import org.molgenis.data.aggregation.AggregateAnonymizerRepositoryDecorator;
import org.molgenis.data.cache.l1.L1Cache;
import org.molgenis.data.cache.l1.L1CacheRepositoryDecorator;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l2.L2CacheRepositoryDecorator;
import org.molgenis.data.cache.l3.L3Cache;
import org.molgenis.data.cache.l3.L3CacheRepositoryDecorator;
import org.molgenis.data.elasticsearch.IndexedRepositoryDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenerRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.Tag;
import org.molgenis.data.meta.system.SystemEntityTypeRegistry;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionalRepositoryDecorator;
import org.molgenis.data.validation.*;
import org.molgenis.data.validation.meta.*;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.security.owned.OwnedEntityType.OWNED;

@Component
public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	private final EntityManager entityManager;
	private final EntityAttributesValidator entityAttributesValidator;
	private final AggregateAnonymizer aggregateAnonymizer;
	private final AppSettings appSettings;
	private final DataService dataService;
	private final ExpressionValidator expressionValidator;
	private final SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final SystemEntityTypeRegistry systemEntityTypeRegistry;
	private final IndexActionRegisterService indexActionRegisterService;
	private final SearchService searchService;
	private final L1Cache l1Cache;
	private final EntityListenersService entityListenersService;
	private final L2Cache l2Cache;
	private final TransactionInformation transactionInformation;
	private final MolgenisPermissionService permissionService;
	private final EntityTypeValidator entityTypeValidator;
	private final PackageValidator packageValidator;
	private final TagValidator tagValidator;
	private final L3Cache l3Cache;
	private final EntityTypeDependencyResolver entityTypeDependencyResolver;
	private final AttributeValidator attributeValidator;
	private final PlatformTransactionManager transactionManager;
	private final QueryValidator queryValidator;
	private final IdentifierLookupService identifierLookupService;

	@Autowired
	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager,
			EntityAttributesValidator entityAttributesValidator, AggregateAnonymizer aggregateAnonymizer,
			AppSettings appSettings, DataService dataService, ExpressionValidator expressionValidator,
			SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry,
			SystemEntityTypeRegistry systemEntityTypeRegistry,
			IndexActionRegisterService indexActionRegisterService, SearchService searchService, L1Cache l1Cache,
			L2Cache l2Cache, TransactionInformation transactionInformation,
			EntityListenersService entityListenersService, MolgenisPermissionService permissionService,
			EntityTypeValidator entityTypeValidator, PackageValidator packageValidator, TagValidator tagValidator,
			L3Cache l3Cache, EntityTypeDependencyResolver entityTypeDependencyResolver,
			AttributeValidator attributeValidator, PlatformTransactionManager transactionManager,
			QueryValidator queryValidator, IdentifierLookupService identifierLookupService)

	{
		this.entityManager = requireNonNull(entityManager);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.aggregateAnonymizer = requireNonNull(aggregateAnonymizer);
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
		this.expressionValidator = requireNonNull(expressionValidator);
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.systemEntityTypeRegistry = requireNonNull(systemEntityTypeRegistry);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
		this.searchService = requireNonNull(searchService);
		this.l1Cache = requireNonNull(l1Cache);
		this.entityListenersService = requireNonNull(entityListenersService);
		this.l2Cache = requireNonNull(l2Cache);
		this.transactionInformation = requireNonNull(transactionInformation);
		this.permissionService = requireNonNull(permissionService);
		this.entityTypeValidator = requireNonNull(entityTypeValidator);
		this.packageValidator = requireNonNull(packageValidator);
		this.tagValidator = requireNonNull(tagValidator);
		this.l3Cache = requireNonNull(l3Cache);
		this.entityTypeDependencyResolver = requireNonNull(entityTypeDependencyResolver);
		this.attributeValidator = requireNonNull(attributeValidator);
		this.transactionManager = requireNonNull(transactionManager);
		this.queryValidator = requireNonNull(queryValidator);
		this.identifierLookupService = requireNonNull(identifierLookupService);
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repository;

		// 12. Query the L2 cache before querying the database
		decoratedRepository = new L2CacheRepositoryDecorator(decoratedRepository, l2Cache, transactionInformation);

		// 11. Query the L1 cache before querying the database
		decoratedRepository = new L1CacheRepositoryDecorator(decoratedRepository, l1Cache);

		// 10. Route specific queries to the index
		decoratedRepository = new IndexedRepositoryDecorator(decoratedRepository, searchService);

		// 9. Query the L3 cache before querying the index
		decoratedRepository = new L3CacheRepositoryDecorator(decoratedRepository, l3Cache, transactionInformation);

		// 8. Register the cud action needed to index indexed repositories
		decoratedRepository = new IndexActionRepositoryDecorator(decoratedRepository, indexActionRegisterService);

		// 7. Custom decorators
		decoratedRepository = applyCustomRepositoryDecorators(decoratedRepository);

		// 6. Owned decorator
		if (EntityUtils.doesExtend(decoratedRepository.getEntityType(), OWNED))
		{
			decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
		}

		// 5. Entity reference resolver decorator
		decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

		// 4. Entity listener
		decoratedRepository = new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

		// 3. validation decorator
		decoratedRepository = new RepositoryValidationDecorator(dataService, decoratedRepository,
				entityAttributesValidator, expressionValidator);

		// 2. aggregate anonymization decorator
		decoratedRepository = new AggregateAnonymizerRepositoryDecorator<>(decoratedRepository, aggregateAnonymizer,
				appSettings);

		// 1. security decorator
		decoratedRepository = new RepositorySecurityDecorator(decoratedRepository);

		// 0. transaction decorator
		decoratedRepository = new TransactionalRepositoryDecorator<>(decoratedRepository, transactionManager);

		// -1. query validation decorator
		decoratedRepository = new QueryValidationRepositoryDecorator<>(decoratedRepository, queryValidator);

		return decoratedRepository;
	}

	/**
	 * Apply custom repository decorators based on entity meta data
	 *
	 * @param repo entity repository
	 * @return decorated entity repository
	 */
	@SuppressWarnings("unchecked")
	private Repository<Entity> applyCustomRepositoryDecorators(Repository<Entity> repo)
	{
		switch (repo.getName())
		{
			case ATTRIBUTE_META_DATA:
				repo = (Repository<Entity>) (Repository<? extends Entity>) new AttributeRepositoryDecorator(
						(Repository<Attribute>) (Repository<? extends Entity>) repo, systemEntityTypeRegistry,
						dataService, permissionService);
				return (Repository<Entity>) (Repository<? extends Entity>) new AttributeRepositoryValidationDecorator(
						(Repository<Attribute>) (Repository<? extends Entity>) repo, attributeValidator);
			case ENTITY_TYPE_META_DATA:
				repo = (Repository<Entity>) (Repository<? extends Entity>) new EntityTypeRepositoryDecorator(
						(Repository<EntityType>) (Repository<? extends Entity>) repo, dataService,
						systemEntityTypeRegistry, permissionService, identifierLookupService);
				return (Repository<Entity>) (Repository<? extends Entity>) new EntityTypeRepositoryValidationDecorator(
						(Repository<EntityType>) (Repository<? extends Entity>) repo, entityTypeValidator);
			case PACKAGE:
				repo = (Repository<Entity>) (Repository<? extends Entity>) new PackageRepositoryDecorator(
						(Repository<Package>) (Repository<? extends Entity>) repo, dataService,
						entityTypeDependencyResolver);
				return (Repository<Entity>) (Repository<? extends Entity>) new PackageRepositoryValidationDecorator(
						(Repository<Package>) (Repository<? extends Entity>) repo, packageValidator);
			case TAG:
				return (Repository<Entity>) (Repository<? extends Entity>) new TagRepositoryValidationDecorator(
						(Repository<Tag>) (Repository<? extends Entity>) repo, tagValidator);
			default:
				return repositoryDecoratorRegistry.decorate(repo);
		}
	}
}
