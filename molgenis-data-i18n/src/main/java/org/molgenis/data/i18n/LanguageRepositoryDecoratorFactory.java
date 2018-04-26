package org.molgenis.data.i18n;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.i18n.model.LanguageMetadata;
import org.springframework.stereotype.Component;

@Component
public class LanguageRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<Language, LanguageMetadata>
{
	public LanguageRepositoryDecoratorFactory(LanguageMetadata languageMetadata)
	{
		super(languageMetadata);
	}

	@Override
	public Repository<Language> createDecoratedRepository(Repository<Language> repository)
	{
		return new LanguageRepositoryDecorator(repository);
	}
}
