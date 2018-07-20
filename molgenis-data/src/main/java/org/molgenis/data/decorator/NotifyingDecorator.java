package org.molgenis.data.decorator;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Decorator that notifies a listener of mutations that happen to the decorated repository.
 *
 * @param <E> the class of the entities in the repository
 */
public class NotifyingDecorator<E extends Entity> extends AbstractRepositoryDecorator<E>
{
	private final RepositoryListener<E> repositoryListener;

	public NotifyingDecorator(Repository<E> delegateRepository, RepositoryListener<E> repositoryListener)
	{
		super(delegateRepository);
		this.repositoryListener = requireNonNull(repositoryListener);
	}

	@Override
	public void add(E entity)
	{
		repositoryListener.beforeAdd(entity);
		delegate().add(entity);
	}

	@Override
	public Integer add(Stream<E> entities)
	{
		return delegate().add(peek(entities, repositoryListener::beforeAdd));
	}

	@Override
	public void update(E entity)
	{
		repositoryListener.beforeUpdate(entity);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<E> entities)
	{
		delegate().update(entities.filter((E entity) ->
		{
			repositoryListener.beforeUpdate(entity);
			return true;
		}));
	}

	@Override
	public void delete(E entity)
	{
		repositoryListener.beforeDelete(entity);
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<E> entities)
	{
		deleteAll(entities.map(Entity::getIdValue));
	}

	@Override
	public void deleteById(Object id)
	{
		repositoryListener.beforeDelete(id);
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		delegate().deleteAll(peek(ids, repositoryListener::beforeDelete));
	}

	@Override
	public void deleteAll()
	{
		deleteAll(
				peek(delegate().findAll(new QueryImpl<>()).map(Entity::getIdValue), repositoryListener::beforeDelete));
	}

	/**
	 * Peeks at a stream, calling a consumer for each element.
	 *
	 * @param stream   the {@link Stream} to peek at
	 * @param consumer the {@link Consumer} to call for each element
	 * @param <T>      the type of the elements in the stream
	 * @return the stream with the peeking consumer
	 * <p>
	 * @see Stream.peek
	 */
	private static <T> Stream<T> peek(Stream<T> stream, Consumer<T> consumer)
	{
		return stream.filter(t ->
		{
			consumer.accept(t);
			return true;
		});
	}
}
