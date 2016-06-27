package org.molgenis.ui;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserDecorator;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.*;
import org.molgenis.data.cache.L1Cache;
import org.molgenis.data.cache.L1CacheRepositoryDecorator;
import org.molgenis.data.elasticsearch.IndexedRepositoryDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.i18n.I18nStringDecorator;
import org.molgenis.data.i18n.Language;
import org.molgenis.data.i18n.LanguageRepositoryDecorator;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecorator;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecorator;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.data.reindex.ReindexActionRepositoryDecorator;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.data.i18n.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.security.owned.OwnedEntityMetaData.OWNED;

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
	private final UserAuthorityFactory userAuthorityFactory;
	private final ReindexActionRegisterService reindexActionRegisterService;
	private final SearchService searchService;
	private final AttributeMetaDataFactory attrMetaFactory;
	private final PasswordEncoder passwordEncoder;
	private final L1Cache l1Cache;

	@Autowired
	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager,
			EntityAttributesValidator entityAttributesValidator, IdGenerator idGenerator, AppSettings appSettings,
			DataService dataService, ExpressionValidator expressionValidator,
			RepositoryDecoratorRegistry repositoryDecoratorRegistry,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, UserAuthorityFactory userAuthorityFactory,
			ReindexActionRegisterService reindexActionRegisterService, SearchService searchService,
			AttributeMetaDataFactory attrMetaFactory, PasswordEncoder passwordEncoder, L1Cache l1Cache)
	{
		this.entityManager = requireNonNull(entityManager);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.idGenerator = requireNonNull(idGenerator);
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
		this.expressionValidator = requireNonNull(expressionValidator);
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.reindexActionRegisterService = requireNonNull(reindexActionRegisterService);
		this.searchService = requireNonNull(searchService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.passwordEncoder = requireNonNull(passwordEncoder);
		this.l1Cache = requireNonNull(l1Cache);
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repositoryDecoratorRegistry.decorate(repository);

		// 11. Query the L1 cache before asking the database
		decoratedRepository = new L1CacheRepositoryDecorator(decoratedRepository, l1Cache);

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

	/**
	 * Apply custom repository decorators based on entity meta data
	 *
	 * @param repo entity repository
	 * @return decorated entity repository
	 */
	private Repository<Entity> applyCustomRepositoryDecorators(Repository<Entity> repo)
	{
		if (repo.getName().equals(MOLGENIS_USER))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new MolgenisUserDecorator(
					(Repository<MolgenisUser>) (Repository<? extends Entity>) repo, userAuthorityFactory, dataService,
					passwordEncoder);
		}
		else if (repo.getName().equals(ATTRIBUTE_META_DATA))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new AttributeMetaDataRepositoryDecorator(
					(Repository<AttributeMetaData>) (Repository<? extends Entity>) repo, systemEntityMetaDataRegistry,
					dataService);
		}
		else if (repo.getName().equals(ENTITY_META_DATA))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new EntityMetaDataRepositoryDecorator(
					(Repository<EntityMetaData>) (Repository<? extends Entity>) repo, dataService,
					systemEntityMetaDataRegistry);
		}
		else if (repo.getName().equals(PACKAGE))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new PackageRepositoryDecorator(
					(Repository<Package>) (Repository<? extends Entity>) repo, dataService);
		}
		else if (repo.getName().equals(LANGUAGE))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new LanguageRepositoryDecorator(
					(Repository<Language>) (Repository<? extends Entity>) repo, dataService,
					systemEntityMetaDataRegistry, attrMetaFactory);
		}
		else if (repo.getName().equals(I18N_STRING))
		{
			repo = new I18nStringDecorator(repo);
		}
		return repo;
	}
}
