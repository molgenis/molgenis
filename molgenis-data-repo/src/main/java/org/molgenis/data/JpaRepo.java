package org.molgenis.data;

import java.io.FileNotFoundException;

import javax.persistence.EntityManager;

import org.molgenis.data.jpa.JpaRepository;
import org.molgenis.data.support.AbstractRepo;
import org.molgenis.data.support.ConvertingIterable;

public class JpaRepo extends AbstractRepo implements Repo, QueryableRepo, CrudRepo
{
	JpaRepository<Entity> repository;

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public JpaRepo(EntityManager em, Class<? extends Entity> entityClass) throws FileNotFoundException
	{
		this.repository = new JpaRepository(em, entityClass);
		super.repository = repository;
	}

	@Override
	public long count(Query q)
	{
		return repository.count();
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return repository.findAll(q);
	}

	@Override
	public Entity findOne(Query q)
	{
		return repository.findOne(q);
	}

	@Override
	public Entity findOne(Integer id)
	{
		return repository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		return repository.findAll(ids);
	}

	@Override
	public long count()
	{
		return repository.count();
	}

	@Override
	public void update(Entity entity)
	{
		repository.update(entity);
	}

	@Override
	public void update(Iterable<Entity> records)
	{
		repository.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		repository.delete(entity);
	}

	@Override
	public void delete(Iterable<Entity> entities)
	{
		repository.delete(entities);
	}

	@Override
	public void deleteById(Integer id)
	{
		repository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		repository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
	repository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		repository.add(entity);
	}

	@Override
	public void add(Iterable<Entity> entities)
	{
		repository.add(entities);
	}

	@Override
	public void flush()
	{
		repository.flush();
	}

	@Override
	public void clearCache()
	{
		repository.clearCache();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> entityClass)
	{
		return (Iterable<E>) new ConvertingIterable<E>(entityClass, repository.findAll(q));
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> entityClass)
	{
		return (Iterable<E>) new ConvertingIterable<E>(entityClass, repository.findAll(ids));
	}
}
