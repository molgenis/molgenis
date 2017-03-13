package org.molgenis.data.i18n;

import org.molgenis.data.Repository;
import org.molgenis.data.StaticEntityRepositoryDecoratorFactory;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class LanguageRepositoryDecoratorFactory
		extends StaticEntityRepositoryDecoratorFactory<Language, LanguageMetadata>
{
	private final LanguageService languageService;

	public LanguageRepositoryDecoratorFactory(LanguageMetadata languageMetadata, LanguageService languageService)
	{
		super(languageMetadata);
		this.languageService = requireNonNull(languageService);
	}

	@Override
	public Repository<Language> createDecoratedRepository(Repository<Language> repository)
	{
		return new LanguageRepositoryDecorator(repository, languageService);
	}
}
