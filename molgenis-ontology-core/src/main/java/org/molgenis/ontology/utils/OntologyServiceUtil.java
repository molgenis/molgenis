package org.molgenis.ontology.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;

public class OntologyServiceUtil
{
	public static List<Map<String, Object>> getEntityAsMap(Iterable<Entity> entities)
	{
		List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>();
		for (Entity entity : entities)
		{
			docs.add(getEntityAsMap(entity));
		}
		return docs;
	}

	public static Map<String, Object> getEntityAsMap(Entity entity)
	{
		if (entity == null) return Collections.emptyMap();
		Map<String, Object> doc = new LinkedHashMap<String, Object>();
		for (String attrName : entity.getAttributeNames())
		{
			doc.put(attrName, entity.get(attrName));
		}
		return doc;
	}
}
