package org.molgenis.ontology.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.ontology.beans.ComparableEntity;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class PostProcessRemoveRedundantOntologyTerm
{
	public static void process(List<ComparableEntity> comparableEntities)
	{
		Map<String, ComparableEntity> redundantEntities = new LinkedHashMap<String, ComparableEntity>();
		for (ComparableEntity entity : comparableEntities)
		{
			String ontologyTermIri = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
			if (!redundantEntities.containsKey(ontologyTermIri))
			{
				redundantEntities.put(ontologyTermIri, entity);
			}
		}
		comparableEntities.clear();
		comparableEntities.addAll(redundantEntities.values());
	}
}