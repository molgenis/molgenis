package org.molgenis.data;


/**
 * Generic <Entity> version of the Repository api. This alternative api doesn't require extensive use of '<Xyz>'
 */
public interface CrudRepo
{
	public void add(Entity entity);
	
	public void add(Iterable<Entity> entities);
	
	public void update(Entity entity);
	
	public void update(Iterable<Entity> records);
	
	public void delete(Entity entity);
	
	public void delete(Iterable<Entity> entities);
	
	public void deleteById(Integer id);
	
	public void deleteById(Iterable<Integer> ids);
	
	public void deleteAll();
	
	public void flush();
	
	public void clearCache();
}
