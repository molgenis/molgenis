package org.molgenis.ui;

import org.molgenis.auth.MolgenisUserDecorator;
import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.AutoValueRepositoryDecorator;
import org.molgenis.data.ComputedEntityValuesDecorator;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityManager;
import org.molgenis.data.EntityReferenceResolverDecorator;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositorySecurityDecorator;
import org.molgenis.data.RowLevelSecurityPermissionValidator;
import org.molgenis.data.RowLevelSecurityRepositoryDecorator;
import org.molgenis.data.mysql.MysqlRepositoryCollection;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.transaction.TransactionLogRepositoryDecorator;
import org.molgenis.data.transaction.TransactionLogService;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.MySqlRepositoryExceptionTranslatorDecorator;

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
	private final RowLevelSecurityPermissionValidator rowLevelSecurityPermissionValidator;

	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager, TransactionLogService transactionLogService,
			EntityAttributesValidator entityAttributesValidator, IdGenerator idGenerator, AppSettings appSettings,
			DataService dataService, ExpressionValidator expressionValidator,
			RepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{
		this.entityManager = entityManager;
		this.transactionLogService = transactionLogService;
		this.entityAttributesValidator = entityAttributesValidator;
		this.idGenerator = idGenerator;
		this.appSettings = appSettings;
		this.dataService = dataService;
		this.expressionValidator = expressionValidator;
		this.repositoryDecoratorRegistry = repositoryDecoratorRegistry;
		this.rowLevelSecurityPermissionValidator = new RowLevelSecurityPermissionValidator(dataService);
	}

	@Override
	public Repository createDecoratedRepository(Repository repository)
	{
		Repository decoratedRepository = repositoryDecoratorRegistry.decorate(repository);

		// 1. Row level security decorator
		decoratedRepository = new RowLevelSecurityRepositoryDecorator(decoratedRepository, dataService,
				rowLevelSecurityPermissionValidator);

		if (decoratedRepository.getName().equals(MolgenisUserMetaData.ENTITY_NAME))
		{
			decoratedRepository = new MolgenisUserDecorator(decoratedRepository);
		}

		// 10. Owned decorator
		if (EntityUtils.doesExtend(decoratedRepository.getEntityMetaData(), OwnedEntityMetaData.ENTITY_NAME))
		{
			decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
		}

		// 9. Entity reference resolver decorator
		decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

		// 8. Computed entity values decorator
		decoratedRepository = new ComputedEntityValuesDecorator(decoratedRepository);

		// 7. Entity listener
		decoratedRepository = new EntityListenerRepositoryDecorator(decoratedRepository);

		// 6. Transaction log decorator
		decoratedRepository = new TransactionLogRepositoryDecorator(decoratedRepository, transactionLogService);

		// 5. SQL exception translation decorator
		String backend = decoratedRepository.getEntityMetaData().getBackend();
		if (MysqlRepositoryCollection.NAME.equals(backend))
		{
			decoratedRepository = new MySqlRepositoryExceptionTranslatorDecorator(decoratedRepository);
		}

		// 4. validation decorator
		decoratedRepository = new RepositoryValidationDecorator(dataService, decoratedRepository,
				entityAttributesValidator, expressionValidator);

		// 3. auto value decorator
		decoratedRepository = new AutoValueRepositoryDecorator(decoratedRepository, idGenerator);

		// 2. security decorator
		decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, appSettings);

		return decoratedRepository;
	}
}
