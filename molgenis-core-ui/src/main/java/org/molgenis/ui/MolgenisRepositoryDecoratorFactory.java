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
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecorator;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecorator;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.TypedRepositoryDecorator;
import org.molgenis.data.transaction.TransactionLogService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	private final EntityManager entityManager;
	private final TransactionLogService transactionLogService;
	private final EntityAttributesValidator entityAttributesValidator;
	private final IdGenerator idGenerator;
	private final AppSettings appSettings;
	private final DataService dataService;
	private final ExpressionValidator expressionValidator;
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final LanguageService languageService;

	@Autowired
	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager, TransactionLogService transactionLogService,
			EntityAttributesValidator entityAttributesValidator, IdGenerator idGenerator, AppSettings appSettings,
			DataService dataService, ExpressionValidator expressionValidator,
			RepositoryDecoratorRegistry repositoryDecoratorRegistry, SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, LanguageService languageService)
	{
		this.entityManager = entityManager;
		this.transactionLogService = transactionLogService;
		this.entityAttributesValidator = entityAttributesValidator;
		this.idGenerator = idGenerator;
		this.appSettings = appSettings;
		this.dataService = dataService;
		this.expressionValidator = expressionValidator;
		this.repositoryDecoratorRegistry = repositoryDecoratorRegistry;
		this.systemEntityMetaDataRegistry = systemEntityMetaDataRegistry;
		this.languageService = languageService;
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repositoryDecoratorRegistry.decorate(repository);

		// 9. Custom decorators
		decoratedRepository = applyCustomRepositoryDecorators(decoratedRepository);

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

		// 4. Transaction log decorator
//		decoratedRepository = new TransactionLogRepositoryDecorator(decoratedRepository, transactionLogService);

		// 3. validation decorator
		decoratedRepository = new RepositoryValidationDecorator(dataService, decoratedRepository,
				entityAttributesValidator, expressionValidator);

		// 2. auto value decorator
		decoratedRepository = new AutoValueRepositoryDecorator(decoratedRepository, idGenerator);

		// 1. security decorator
		decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, appSettings);

		return decoratedRepository;
	}

	/**
	 * Apply custom repository decorators based on entity meta data
	 *
	 * @param repository
	 * @return
	 */
	private Repository applyCustomRepositoryDecorators(Repository repository)
	{
		if (repository.getName().equals(MolgenisUserMetaData.ENTITY_NAME))
		{
			repository = new MolgenisUserDecorator(repository);
		}
		else if (repository.getName().equals(AttributeMetaDataMetaData.ENTITY_NAME))
		{
			repository = new AttributeMetaDataRepositoryDecorator(repository, systemEntityMetaDataRegistry);
		}
		else if (repository.getName().equals(EntityMetaDataMetaData.ENTITY_NAME))
		{
			repository = new EntityMetaDataRepositoryDecorator(new TypedRepositoryDecorator<>(repository, EntityMetaData.class), dataService, systemEntityMetaDataRegistry);
		}
//		else if (repository.getName().equals(PackageMetaData.ENTITY_NAME))
//		{
//			repository = new PackageRepositoryDecorator(repository, dataService, systemEntityMetaDataRegistry);
//		}
//		else if (repository.getName().equals(LanguageMetaData.ENTITY_NAME))
//		{
//			repository = new LanguageRepositoryDecorator(repository, dataService);
//		}
//		else if (repository.getName().equals(I18nStringMetaData.ENTITY_NAME))
//		{
//			repository = new I18nStringDecorator(repository);
//		}
//		else if (repository.getName().equals(TagMetaData.ENTITY_NAME))
//		{
//			// do nothing
//		}
		return repository;
	}
}
