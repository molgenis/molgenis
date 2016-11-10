package org.molgenis.data.i18n;

import org.molgenis.data.*;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.EntityType;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class LanguageRepositoryDecorator implements Repository<Language>
{
	private final Repository<Language> decorated;
	private final LanguageService languageService;

	public LanguageRepositoryDecorator(Repository<Language> decorated, LanguageService languageService)
	{
		this.decorated = requireNonNull(decorated);
		this.languageService = requireNonNull(languageService);
	}

	@Override
	public Iterator<Language> iterator()
	{
		return decorated.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Language>> consumer, int batchSize)
	{
		decorated.forEachBatched(fetch, consumer, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decorated.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decorated.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decorated.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decorated.getName();
	}

	public EntityType getEntityType()
	{
		return decorated.getEntityType();
	}

	@Override
	public long count()
	{
		return decorated.count();
	}

	@Override
	public Query<Language> query()
	{
		return decorated.query();
	}

	@Override
	public long count(Query<Language> q)
	{
		return decorated.count(q);
	}

	@Override
	public Stream<Language> findAll(Query<Language> q)
	{
		return decorated.findAll(q);
	}

	@Override
	public Language findOne(Query<Language> q)
	{
		return decorated.findOne(q);
	}

	@Override
	public Language findOneById(Object id)
	{
		return decorated.findOneById(id);
	}

	@Override
	public Language findOneById(Object id, Fetch fetch)
	{
		return decorated.findOneById(id, fetch);
	}

	@Override
	public Stream<Language> findAll(Stream<Object> ids)
	{
		return decorated.findAll(ids);
	}

	@Override
	public Stream<Language> findAll(Stream<Object> ids, Fetch fetch)
	{
		return decorated.findAll(ids, fetch);
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		return decorated.aggregate(aggregateQuery);
	}

	@Override
	public void update(Language entity)
	{
		decorated.update(entity);
	}

	@Override
	public void update(Stream<Language> entities)
	{
		decorated.update(entities);
	}

	@Override
	public void delete(Language language)
	{
		throw new MolgenisDataException(format("Deleting languages is not allowed"));
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
			throw new MolgenisDataException(format("Adding languages is not allowed"));
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
