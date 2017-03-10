package org.molgenis.data.i18n.model;

import org.molgenis.data.RepositoryDecoratorRegistry;
import org.molgenis.data.i18n.I18nStringDecorator;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;

@Configuration
public class I18nStringConfig
{
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	public I18nStringConfig(RepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
	}

	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(I18N_STRING, I18nStringDecorator::new);
	}
}
