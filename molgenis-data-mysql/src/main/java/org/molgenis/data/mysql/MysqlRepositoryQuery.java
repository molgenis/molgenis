package org.molgenis.data.mysql;

import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;

/**
 * Created by mswertz on 01/04/14.
 */
public class MysqlRepositoryQuery extends QueryImpl
{
	MysqlRepository repo;

	protected MysqlRepositoryQuery(MysqlRepository repo)
	{
		this.repo = repo;
	}

	public long count()
	{
		return repo.count(this);
	}

	public Iterable<Entity> findAll()
	{
		return repo.findAll(this);
	}
}
