package org.molgenis.data;

public interface CrudRepository<E extends Entity> extends Repository<E>, Queryable<E>, Updateable
{

}
