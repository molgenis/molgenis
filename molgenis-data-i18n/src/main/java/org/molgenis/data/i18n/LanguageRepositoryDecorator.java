package org.molgenis.data.i18n;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.i18n.LanguageService;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


public class LanguageRepositoryDecorator extends AbstractRepositoryDecorator<Language>
{
	public LanguageRepositoryDecorator(Repository<Language> delegateRepository)
	{
		super(delegateRepository);
	}

	@Override
	public void delete(Language language)
	{
		throw new MolgenisDataException("Deleting languages is not allowed");
	}

	@Override
	public void delete(Stream<Language> entities)
	{
		entities.forEach(this::delete);
	}

	@Override
	public void deleteById(Object id)
	{
		Language entity = findOneById(id);
		if (entity != null) delete(entity);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		ids.forEach(this::deleteById);
	}

	@Override
	public void deleteAll()
	{
		forEachBatched(entities -> delete(entities.stream()), 1000);
	}

	@Override
	public void add(Language language)
	{

		if (!LanguageService.hasLanguageCode(language.getCode()))
		{
			throw new MolgenisDataException("Adding languages is not allowed");
		}
		else
		{
			// Add language
			delegate().add(language);
		}
	}

	@Override
	public Integer add(Stream<Language> entities)
	{
		AtomicInteger count = new AtomicInteger();
		entities.forEach(entity ->
		{
			add(entity); // FIXME inefficient, apply filter to stream
			count.incrementAndGet();
		});
		return count.get();
	}
}
