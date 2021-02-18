package org.molgenis.data.platform.decorators;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.semantic.Vocabulary.AUDITED;
import static org.molgenis.data.util.EntityTypeUtils.isSystemEntity;

import org.molgenis.audit.AuditEventPublisher;
import org.molgenis.data.CascadeDeleteRepositoryDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityReferenceResolverDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.SystemRepositoryDecoratorRegistry;
import org.molgenis.data.cache.l1.L1Cache;
import org.molgenis.data.cache.l1.L1CacheJanitor;
import org.molgenis.data.cache.l1.L1CacheRepositoryDecorator;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l2.L2CacheRepositoryDecorator;
import org.molgenis.data.cache.l3.L3Cache;
import org.molgenis.data.cache.l3.L3CacheRepositoryDecorator;
import org.molgenis.data.decorator.DynamicRepositoryDecoratorRegistry;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryDecorator;
import org.molgenis.data.index.IndexedRepositoryDecoratorFactory;
import org.molgenis.data.listeners.EntityListenerRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.RepositorySecurityDecorator;
import org.molgenis.data.security.aggregation.AggregateAnonymizer;
import org.molgenis.data.security.aggregation.AggregateAnonymizerRepositoryDecorator;
import org.molgenis.data.security.audit.AuditingRepositoryDecorator;
import org.molgenis.data.security.owned.RowLevelSecurityRepositoryDecoratorFactory;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionalRepositoryDecorator;
import org.molgenis.data.validation.DefaultValueReferenceValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.FetchValidator;
import org.molgenis.data.validation.QueryValidationRepositoryDecorator;
import org.molgenis.data.validation.QueryValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory {

  private final EntityManager entityManager;
  private final EntityAttributesValidator entityAttributesValidator;
  private final AggregateAnonymizer aggregateAnonymizer;
  private final AppSettings appSettings;
  private final DataService dataService;
  private final SystemRepositoryDecoratorRegistry systemRepositoryDecoratorRegistry;
  private final DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry;
  private final IndexActionRegisterService indexActionRegisterService;
  private final IndexedRepositoryDecoratorFactory indexedRepositoryDecoratorFactory;
  private final L1Cache l1Cache;
  private final EntityListenersService entityListenersService;
  private final L2Cache l2Cache;
  private final TransactionInformation transactionInformation;
  private final L3Cache l3Cache;
  private final PlatformTransactionManager transactionManager;
  private final QueryValidator queryValidator;
  private final FetchValidator fetchValidator;
  private final DefaultValueReferenceValidator defaultValueReferenceValidator;
  private final UserPermissionEvaluator permissionService;
  private final RowLevelSecurityRepositoryDecoratorFactory
      rowLevelSecurityRepositoryDecoratorFactory;
  private final L1CacheJanitor l1CacheJanitor;
  private final AuditEventPublisher auditEventPublisher;

  public MolgenisRepositoryDecoratorFactory(
      EntityManager entityManager,
      EntityAttributesValidator entityAttributesValidator,
      AggregateAnonymizer aggregateAnonymizer,
      AppSettings appSettings,
      DataService dataService,
      SystemRepositoryDecoratorRegistry repositoryDecoratorRegistry,
      DynamicRepositoryDecoratorRegistry dynamicRepositoryDecoratorRegistry,
      IndexActionRegisterService indexActionRegisterService,
      IndexedRepositoryDecoratorFactory indexedRepositoryDecoratorFactory,
      L1Cache l1Cache,
      L2Cache l2Cache,
      TransactionInformation transactionInformation,
      EntityListenersService entityListenersService,
      L3Cache l3Cache,
      PlatformTransactionManager transactionManager,
      QueryValidator queryValidator,
      FetchValidator fetchValidator,
      DefaultValueReferenceValidator defaultValueReferenceValidator,
      UserPermissionEvaluator permissionService,
      RowLevelSecurityRepositoryDecoratorFactory rowLevelSecurityRepositoryDecoratorFactory,
      L1CacheJanitor l1CacheJanitor,
      AuditEventPublisher auditEventPublisher) {

    this.entityManager = requireNonNull(entityManager);
    this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
    this.aggregateAnonymizer = requireNonNull(aggregateAnonymizer);
    this.appSettings = requireNonNull(appSettings);
    this.dataService = requireNonNull(dataService);
    this.systemRepositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
    this.dynamicRepositoryDecoratorRegistry = requireNonNull(dynamicRepositoryDecoratorRegistry);
    this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
    this.indexedRepositoryDecoratorFactory = requireNonNull(indexedRepositoryDecoratorFactory);
    this.l1Cache = requireNonNull(l1Cache);
    this.entityListenersService = requireNonNull(entityListenersService);
    this.l2Cache = requireNonNull(l2Cache);
    this.transactionInformation = requireNonNull(transactionInformation);
    this.l3Cache = requireNonNull(l3Cache);
    this.transactionManager = requireNonNull(transactionManager);
    this.queryValidator = requireNonNull(queryValidator);
    this.fetchValidator = requireNonNull(fetchValidator);
    this.defaultValueReferenceValidator = requireNonNull(defaultValueReferenceValidator);
    this.permissionService = requireNonNull(permissionService);
    this.rowLevelSecurityRepositoryDecoratorFactory =
        requireNonNull(rowLevelSecurityRepositoryDecoratorFactory);
    this.l1CacheJanitor = requireNonNull(l1CacheJanitor);
    this.auditEventPublisher = requireNonNull(auditEventPublisher);
  }

  @Override
  public Repository<Entity> createDecoratedRepository(Repository<Entity> repository) {
    Repository<Entity> decoratedRepository = repository;

    // 16. Query the L2 cache before querying the database
    decoratedRepository =
        new L2CacheRepositoryDecorator(decoratedRepository, l2Cache, transactionInformation);

    // 15. Query the L1 cache before querying the database
    decoratedRepository =
        new L1CacheRepositoryDecorator(decoratedRepository, l1Cache, l1CacheJanitor);

    // 14. Route specific queries to the index
    decoratedRepository = indexedRepositoryDecoratorFactory.create(decoratedRepository);

    // 13. Query the L3 cache before querying the index
    decoratedRepository =
        new L3CacheRepositoryDecorator(decoratedRepository, l3Cache, transactionInformation);

    // 12. Register the cud action needed to index indexed repositories
    decoratedRepository =
        new IndexActionRepositoryDecorator(decoratedRepository, indexActionRegisterService);

    // 11. Custom decorators for system entity types
    decoratedRepository = systemRepositoryDecoratorRegistry.decorate(decoratedRepository);

    // 10. Perform cascading deletes
    decoratedRepository = new CascadeDeleteRepositoryDecorator(decoratedRepository, dataService);

    // 9. Row level security decorator
    decoratedRepository =
        rowLevelSecurityRepositoryDecoratorFactory.createDecoratedRepository(decoratedRepository);

    // 8. Entity reference resolver decorator
    decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

    // 7. Entity listener
    decoratedRepository =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

    // 6. validation decorator
    decoratedRepository =
        new RepositoryValidationDecorator(
            dataService,
            decoratedRepository,
            entityAttributesValidator,
            defaultValueReferenceValidator);

    // 5. aggregate anonymization decorator
    decoratedRepository =
        new AggregateAnonymizerRepositoryDecorator<>(
            decoratedRepository, aggregateAnonymizer, appSettings);

    // 4. security decorator
    decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, permissionService);

    // 3. transaction decorator
    decoratedRepository =
        new TransactionalRepositoryDecorator<>(decoratedRepository, transactionManager);

    // 2. query validation decorator
    decoratedRepository =
        new QueryValidationRepositoryDecorator<>(
            decoratedRepository, queryValidator, fetchValidator);

    // 1. Data auditing decorator
    if (isAuditedEntityType(decoratedRepository.getEntityType())) {
      decoratedRepository =
          new AuditingRepositoryDecorator(decoratedRepository, auditEventPublisher);
    }

    // 0. Dynamic decorators
    decoratedRepository = dynamicRepositoryDecoratorRegistry.decorate(decoratedRepository);

    return decoratedRepository;
  }

  private static boolean isAuditedEntityType(EntityType entityType) {
    return isSystemEntity(entityType)
        || stream(entityType.getTags())
            .anyMatch(tag -> AUDITED.toString().equals(tag.getObjectIri()));
  }
}
