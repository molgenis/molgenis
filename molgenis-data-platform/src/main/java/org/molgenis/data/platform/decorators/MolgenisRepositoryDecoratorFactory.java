package org.molgenis.data.platform.decorators;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.index.job.IndexJobExecutionMetadata.INDEX_JOB_EXECUTION;
import static org.molgenis.data.index.meta.IndexActionGroupMetadata.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.security.audit.AuditSettingsImpl.AUDIT_SETTINGS;

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
import org.molgenis.data.security.RepositorySecurityDecorator;
import org.molgenis.data.security.aggregation.AggregateAnonymizer;
import org.molgenis.data.security.aggregation.AggregateAnonymizerRepositoryDecorator;
import org.molgenis.data.security.owned.RowLevelSecurityRepositoryDecoratorFactory;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionalRepositoryDecorator;
import org.molgenis.data.validation.DefaultValueReferenceValidator;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.FetchValidator;
import org.molgenis.data.validation.QueryValidationRepositoryDecorator;
import org.molgenis.data.validation.QueryValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.audit.SettingsAuditingRepositoryDecoratorFactory;
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
  private final AuditingRepositoryDecoratorFactory auditingRepositoryDecoratorFactory;
  private final SettingsAuditingRepositoryDecoratorFactory
      settingsAuditingRepositoryDecoratorFactory;

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
      AuditingRepositoryDecoratorFactory auditingRepositoryDecoratorFactory,
      SettingsAuditingRepositoryDecoratorFactory settingsAuditingRepositoryDecoratorFactory) {

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
    this.auditingRepositoryDecoratorFactory = requireNonNull(auditingRepositoryDecoratorFactory);
    this.settingsAuditingRepositoryDecoratorFactory =
        requireNonNull(settingsAuditingRepositoryDecoratorFactory);

    dynamicRepositoryDecoratorRegistry.excludeEntityType(AUDIT_SETTINGS);

    auditingRepositoryDecoratorFactory.excludeEntityType(INDEX_JOB_EXECUTION);
    auditingRepositoryDecoratorFactory.excludeEntityType(INDEX_ACTION);
    auditingRepositoryDecoratorFactory.excludeEntityType(INDEX_ACTION_GROUP);
  }

  @Override
  public Repository<Entity> createDecoratedRepository(Repository<Entity> repository) {
    Repository<Entity> decoratedRepository = repository;

    // Query the L2 cache before querying the database
    decoratedRepository =
        new L2CacheRepositoryDecorator(decoratedRepository, l2Cache, transactionInformation);

    // Query the L1 cache before querying the database
    decoratedRepository =
        new L1CacheRepositoryDecorator(decoratedRepository, l1Cache, l1CacheJanitor);

    // Route specific queries to the index
    decoratedRepository = indexedRepositoryDecoratorFactory.create(decoratedRepository);

    // Query the L3 cache before querying the index
    decoratedRepository =
        new L3CacheRepositoryDecorator(decoratedRepository, l3Cache, transactionInformation);

    // Register the cud action needed to index indexed repositories
    decoratedRepository =
        new IndexActionRepositoryDecorator(decoratedRepository, indexActionRegisterService);

    // Custom decorators for system entity types
    decoratedRepository = systemRepositoryDecoratorRegistry.decorate(decoratedRepository);

    // Perform cascading deletes
    decoratedRepository = new CascadeDeleteRepositoryDecorator(decoratedRepository, dataService);

    // Row level security decorator
    decoratedRepository =
        rowLevelSecurityRepositoryDecoratorFactory.createDecoratedRepository(decoratedRepository);

    // Entity reference resolver decorator
    decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

    // Entity listener
    decoratedRepository =
        new EntityListenerRepositoryDecorator(decoratedRepository, entityListenersService);

    // Validation decorator
    decoratedRepository =
        new RepositoryValidationDecorator(
            dataService,
            decoratedRepository,
            entityAttributesValidator,
            defaultValueReferenceValidator);

    // Aggregate anonymization decorator
    decoratedRepository =
        new AggregateAnonymizerRepositoryDecorator<>(
            decoratedRepository, aggregateAnonymizer, appSettings);

    // Data auditing decorator
    decoratedRepository = auditingRepositoryDecoratorFactory.create(decoratedRepository);

    // Settings auditing decorator
    decoratedRepository = settingsAuditingRepositoryDecoratorFactory.decorate(decoratedRepository);

    // Security decorator
    decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, permissionService);

    // Transaction decorator
    decoratedRepository =
        new TransactionalRepositoryDecorator<>(decoratedRepository, transactionManager);

    // Query validation decorator
    decoratedRepository =
        new QueryValidationRepositoryDecorator<>(
            decoratedRepository, queryValidator, fetchValidator);

    // Dynamic decorators
    decoratedRepository = dynamicRepositoryDecoratorRegistry.decorate(decoratedRepository);

    return decoratedRepository;
  }
}
