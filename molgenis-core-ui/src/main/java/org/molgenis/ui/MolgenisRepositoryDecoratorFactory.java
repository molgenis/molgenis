package org.molgenis.ui;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.data.i18n.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.meta.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.PackageMetaData.PACKAGE;
import static org.molgenis.data.support.OwnedEntityMetaData.OWNED;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserDecorator;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.auth.UserAuthorityFactory;
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
import org.molgenis.data.elasticsearch.IndexedRepositoryDecorator;
import org.molgenis.data.elasticsearch.reindex.ReindexActionRegisterService;
import org.molgenis.data.elasticsearch.reindex.ReindexActionRepositoryDecorator;
import org.molgenis.data.i18n.I18nStringDecorator;
import org.molgenis.data.i18n.Language;
import org.molgenis.data.i18n.LanguageRepositoryDecorator;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecorator;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecorator;
import org.molgenis.data.meta.Package;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.support.TypedRepositoryDecorator;
import org.molgenis.data.support.UntypedRepositoryDecorator;
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
	private final EntityAttributesValidator entityAttributesValidator;
	private final IdGenerator idGenerator;
	private final AppSettings appSettings;
	private final DataService dataService;
	private final ExpressionValidator expressionValidator;
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final MolgenisUserFactory molgenisUserFactory;
	private final UserAuthorityFactory userAuthorityFactory;
	private final ReindexActionRegisterService reindexActionRegisterService;

	@Autowired
	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager,
			EntityAttributesValidator entityAttributesValidator, IdGenerator idGenerator, AppSettings appSettings,
			DataService dataService, ExpressionValidator expressionValidator,
			RepositoryDecoratorRegistry repositoryDecoratorRegistry,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, MolgenisUserFactory molgenisUserFactory,
			UserAuthorityFactory userAuthorityFactory, ReindexActionRegisterService reindexActionRegisterService)
	{
		this.entityManager = requireNonNull(entityManager);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.idGenerator = requireNonNull(idGenerator);
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
		this.expressionValidator = requireNonNull(expressionValidator);
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.molgenisUserFactory = requireNonNull(molgenisUserFactory);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repositoryDecoratorRegistry.decorate(repository);

			// 10. Route specific queries to the index
			decoratedRepository = new IndexedRepositoryDecorator(decoratedRepository, searchService);

			// 9. Register the cud action needed to reindex indexed repositories
			decoratedRepository = new ReindexActionRepositoryDecorator(decoratedRepository, reindexActionRegisterService);

		// 8. Custom decorators
		decoratedRepository = applyCustomRepositoryDecorators(decoratedRepository);

		// 7. Owned decorator
		if (EntityUtils.doesExtend(decoratedRepository.getEntityMetaData(), OWNED))
		{
			decoratedRepository = new OwnedEntityRepositoryDecorator(decoratedRepository);
		}

		// 6. Entity reference resolver decorator
		decoratedRepository = new EntityReferenceResolverDecorator(decoratedRepository, entityManager);

		// 5. Computed entity values decorator
		decoratedRepository = new ComputedEntityValuesDecorator(decoratedRepository);

		// 4. Entity listener
		decoratedRepository = new EntityListenerRepositoryDecorator(decoratedRepository);

		// 3. validation decorator
		decoratedRepository = new RepositoryValidationDecorator(dataService, decoratedRepository,
				entityAttributesValidator, expressionValidator);

		// 2. auto value decorator
		decoratedRepository = new AutoValueRepositoryDecorator(decoratedRepository, idGenerator);

		// 1. security decorator
		decoratedRepository = new RepositorySecurityDecorator(decoratedRepository, appSettings);

		return decoratedRepository;
	}

	@Override
	public <E extends Entity> Repository<E> createDecoratedRepository(Repository<E> repository, Class<E> clazz)
	{
		Repository<Entity> decoratedRepository = createDecoratedRepository((Repository<Entity>) repository);
		return new TypedRepositoryDecorator<>(decoratedRepository, clazz);
	}

	/**
	 * Apply custom repository decorators based on entity meta data
	 *
	 * @param repository
	 * @return
	 */
	private Repository applyCustomRepositoryDecorators(Repository<Entity> repository)
	{
		if (repository.getName().equals(MOLGENIS_USER))
		{
			repository = new UntypedRepositoryDecorator<>(
					new MolgenisUserDecorator(new TypedRepositoryDecorator<>(repository, MolgenisUser.class),
							molgenisUserFactory, userAuthorityFactory), MolgenisUser.class);
		}
		else if (repository.getName().equals(ATTRIBUTE_META_DATA))
		{
			repository = new UntypedRepositoryDecorator<>(new AttributeMetaDataRepositoryDecorator(
					new TypedRepositoryDecorator<>(repository, AttributeMetaData.class), systemEntityMetaDataRegistry,
					dataService), AttributeMetaData.class);
		}
		else if (repository.getName().equals(ENTITY_META_DATA))
		{
			// FIXME hacky, remove EntityMetaData interface?
			repository = new UntypedRepositoryDecorator<>(
					(Repository<EntityMetaDataImpl>) (Repository<? extends Entity>) new EntityMetaDataRepositoryDecorator(
							(TypedRepositoryDecorator<EntityMetaData>) (TypedRepositoryDecorator<? extends EntityMetaData>) new TypedRepositoryDecorator<>(
									repository, EntityMetaDataImpl.class), dataService, systemEntityMetaDataRegistry),
					EntityMetaDataImpl.class);
		}
		else if (repository.getName().equals(PACKAGE))
		{
			repository = new UntypedRepositoryDecorator<>(
					new PackageRepositoryDecorator(new TypedRepositoryDecorator<>(repository, Package.class),
							dataService), Package.class);
		}
		else if (repository.getName().equals(LANGUAGE))
		{
			repository = new UntypedRepositoryDecorator<>(
					new LanguageRepositoryDecorator(new TypedRepositoryDecorator<>(repository, Language.class),
							dataService, systemEntityMetaDataRegistry), Language.class);
		}
		else if (repository.getName().equals(I18N_STRING))
		{
			repository = new I18nStringDecorator(repository);
		}
		return repository;
	}
}
