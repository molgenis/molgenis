package org.molgenis.data.i18n;

import org.molgenis.data.Repository;
import org.molgenis.data.StaticEntityRepositoryDecoratorFactory;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.I18nStringMetaData;
import org.springframework.stereotype.Component;

@Component
public class I18nStringRepositoryDecoratorFactory
		extends StaticEntityRepositoryDecoratorFactory<I18nString, I18nStringMetaData>
{
	public I18nStringRepositoryDecoratorFactory(I18nStringMetaData i18nStringMetaData)
	{
		super(i18nStringMetaData);
	}

	@Override
	public Repository<I18nString> createDecoratedRepository(Repository<I18nString> repository)
	{
		return new I18nStringRepositoryDecorator(repository);
	}
}
