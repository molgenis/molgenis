package org.molgenis.data;

public interface EntityListener
{
	public Object getEntityId();

	public void postUpdate(Entity entity);
}
