package org.molgenis.data.mongodb;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;

public interface MongoRepository extends Repository, Writable, Updateable, Countable
{
	Entity findOne(Object id);

	Iterable<Entity> findAll(Iterable<?> ids);
}
