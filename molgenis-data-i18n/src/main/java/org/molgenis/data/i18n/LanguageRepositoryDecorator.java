package org.molgenis.data.i18n;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.i18n.model.Language;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class LanguageRepositoryDecorator extends AbstractRepositoryDecorator<Language>
{
	private final Repository<Language> decorated;
	private final LanguageService languageService;

	public LanguageRepositoryDecorator(Repository<Language> decorated, LanguageService languageService)
	{
		this.decorated = requireNonNull(decorated);
		this.languageService = requireNonNull(languageService);
	}

	@Override
	protected Repository<Language> delegate()
	{
		return decorated;
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

		if (!languageService.hasLanguageCode(language.getCode()))
		{
			throw new MolgenisDataException("Adding languages is not allowed");
		}
		else
		{
			// Add language
			decorated.add(language);
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
