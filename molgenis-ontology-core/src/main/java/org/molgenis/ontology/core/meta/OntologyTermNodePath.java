package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.*;

public class OntologyTermNodePath extends StaticEntity
{
	public OntologyTermNodePath(Entity entity)
	{
		super(entity);
	}

	public OntologyTermNodePath(EntityType entityType)
	{
		super(entityType);
	}

	public OntologyTermNodePath(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getNodePath()
	{
		return getString(NODE_PATH);
	}

	public void setNodePath(String nodePath)
	{
		set(NODE_PATH, nodePath);
	}

	public boolean isRoot()
	{
		Boolean isRoot = getBoolean(ROOT);
		return isRoot != null ? isRoot : false;
	}

	public void setRoot(boolean root)
	{
		set(ROOT, root);
	}
}
