package org.molgenis.data.i18n.model;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorRegistry;
import org.molgenis.data.i18n.LanguageRepositoryDecorator;
import org.molgenis.data.i18n.LanguageService;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;

@Configuration
public class LanguageConfig
{
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final LanguageService languageService;

	public LanguageConfig(RepositoryDecoratorRegistry repositoryDecoratorRegistry, LanguageService languageService)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.languageService = requireNonNull(languageService);
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(LANGUAGE,
				repository -> (Repository<Entity>) (Repository<? extends Entity>) new LanguageRepositoryDecorator(
						(Repository<Language>) (Repository<? extends Entity>) repository, languageService));
	}
}
