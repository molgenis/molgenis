package org.molgenis.data.platform.decorators;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserDecorator;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.*;
import org.molgenis.data.cache.l1.L1Cache;
import org.molgenis.data.cache.l1.L1CacheRepositoryDecorator;
import org.molgenis.data.cache.l2.L2Cache;
import org.molgenis.data.cache.l2.L2CacheRepositoryDecorator;
import org.molgenis.data.cache.l3.L3Cache;
import org.molgenis.data.cache.l3.L3CacheRepositoryDecorator;
import org.molgenis.data.elasticsearch.IndexedRepositoryDecorator;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.i18n.I18nStringDecorator;
import org.molgenis.data.i18n.LanguageRepositoryDecorator;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.index.IndexActionRegisterService;
import org.molgenis.data.index.IndexActionRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenerRepositoryDecorator;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.AttributeMetaDataRepositoryDecorator;
import org.molgenis.data.meta.EntityMetaDataRepositoryDecorator;
import org.molgenis.data.meta.PackageRepositoryDecorator;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.system.SystemEntityMetaDataRegistry;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.ExpressionValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.data.validation.meta.EntityMetaDataRepositoryValidationDecorator;
import org.molgenis.data.validation.meta.EntityMetaDataValidator;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.owned.OwnedEntityRepositoryDecorator;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.molgenis.data.meta.model.PackageMetaData.PACKAGE;
import static org.molgenis.security.owned.OwnedEntityMetaData.OWNED;

@Component
public class MolgenisRepositoryDecoratorFactory implements RepositoryDecoratorFactory
{
	private final EntityManager entityManager;
	private final EntityAttributesValidator entityAttributesValidator;
	private final AppSettings appSettings;
	private final DataService dataService;
	private final ExpressionValidator expressionValidator;
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final SystemEntityMetaDataRegistry systemEntityMetaDataRegistry;
	private final UserAuthorityFactory userAuthorityFactory;
	private final IndexActionRegisterService indexActionRegisterService;
	private final SearchService searchService;
	private final AttributeFactory attrMetaFactory;
	private final PasswordEncoder passwordEncoder;
	private final EntityMetaDataMetaData entityMetaMeta;
	private final I18nStringMetaData i18nStringMeta;
	private final L1Cache l1Cache;
	private final EntityListenersService entityListenersService;
	private final L2Cache l2Cache;
	private final TransactionInformation transactionInformation;
	private final MolgenisPermissionService permissionService;
	private final EntityMetaDataValidator entityMetaDataValidator;
	private final L3Cache l3Cache;
	private final LanguageService languageService;

	@Autowired
	public MolgenisRepositoryDecoratorFactory(EntityManager entityManager,
			EntityAttributesValidator entityAttributesValidator, AppSettings appSettings, DataService dataService,
			ExpressionValidator expressionValidator, RepositoryDecoratorRegistry repositoryDecoratorRegistry,
			SystemEntityMetaDataRegistry systemEntityMetaDataRegistry, UserAuthorityFactory userAuthorityFactory,
			IndexActionRegisterService indexActionRegisterService, SearchService searchService,
			AttributeFactory attrMetaFactory, PasswordEncoder passwordEncoder,
			EntityMetaDataMetaData entityMetaMeta, I18nStringMetaData i18nStringMeta, L1Cache l1Cache, L2Cache l2Cache,
			TransactionInformation transactionInformation, EntityListenersService entityListenersService,
			MolgenisPermissionService permissionService, EntityMetaDataValidator entityMetaDataValidator,
			L3Cache l3Cache, LanguageService languageService)

	{
		this.entityManager = requireNonNull(entityManager);
		this.entityAttributesValidator = requireNonNull(entityAttributesValidator);
		this.appSettings = requireNonNull(appSettings);
		this.dataService = requireNonNull(dataService);
		this.expressionValidator = requireNonNull(expressionValidator);
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.systemEntityMetaDataRegistry = requireNonNull(systemEntityMetaDataRegistry);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.indexActionRegisterService = requireNonNull(indexActionRegisterService);
		this.searchService = requireNonNull(searchService);
		this.attrMetaFactory = requireNonNull(attrMetaFactory);
		this.passwordEncoder = requireNonNull(passwordEncoder);
		this.entityMetaMeta = requireNonNull(entityMetaMeta);
		this.i18nStringMeta = requireNonNull(i18nStringMeta);
		this.l1Cache = requireNonNull(l1Cache);
		this.entityListenersService = requireNonNull(entityListenersService);
		this.l2Cache = requireNonNull(l2Cache);
		this.transactionInformation = requireNonNull(transactionInformation);
		this.permissionService = requireNonNull(permissionService);
		this.entityMetaDataValidator = requireNonNull(entityMetaDataValidator);
		this.l3Cache = requireNonNull(l3Cache);
		this.languageService = requireNonNull(languageService);
	}

	@Override
	public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
	{
		Repository<Entity> decoratedRepository = repositoryDecoratorRegistry.decorate(repository);

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
		if (EntityUtils.doesExtend(decoratedRepository.getEntityMetaData(), OWNED))
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
					(Repository<Attribute>) (Repository<? extends Entity>) repo, systemEntityMetaDataRegistry,
					dataService, permissionService);
		}
		else if (repo.getName().equals(ENTITY_META_DATA))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new EntityMetaDataRepositoryValidationDecorator(
					(Repository<EntityMetaData>) (Repository<? extends Entity>) repo, entityMetaDataValidator);

			repo = (Repository<Entity>) (Repository<? extends Entity>) new EntityMetaDataRepositoryDecorator(
					(Repository<EntityMetaData>) (Repository<? extends Entity>) repo, dataService,
					systemEntityMetaDataRegistry, permissionService);
		}
		else if (repo.getName().equals(PACKAGE))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new PackageRepositoryDecorator(
					(Repository<Package>) (Repository<? extends Entity>) repo, dataService);
		}
		else if (repo.getName().equals(LANGUAGE))
		{
			repo = (Repository<Entity>) (Repository<? extends Entity>) new LanguageRepositoryDecorator(
					(Repository<Language>) (Repository<? extends Entity>) repo, languageService);
		}
		else if (repo.getName().equals(I18N_STRING))
		{
			repo = new I18nStringDecorator(repo);
		}
		return repo;
	}
}
