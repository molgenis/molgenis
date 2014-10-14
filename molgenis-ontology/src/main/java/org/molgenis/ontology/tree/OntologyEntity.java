package org.molgenis.ontology.tree;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Iterables;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semantic.Ontology;
import org.molgenis.data.semantic.OntologyService;
import org.molgenis.data.semantic.OntologyTerm;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class OntologyEntity extends AbstractSemanticEntity implements Ontology
{
	private static final long serialVersionUID = 1L;

	public OntologyEntity(Entity entity, EntityMetaData entityMetaData, OntologyService ontologyService,
			SearchService searchService, DataService dataService)
	{
		super(entity, entityMetaData, searchService, dataService, ontologyService);
	}

	@Override
	public Object get(String attributeName)
	{
		if (attributeName.equalsIgnoreCase(OntologyQueryRepository.FIELDTYPE))
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getLabel());
			Iterable<Entity> listOfOntologyTerms = searchService.search(new QueryImpl().eq(
					OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM),
					entityMetaDataIndexedOntologyTerm);
			return Iterables.size(listOfOntologyTerms) == 0 ? MolgenisFieldTypes.STRING.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND
					.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.LAST))
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(getLabel());
			Iterable<Entity> listOfOntologyTerms = searchService.search(new QueryImpl().eq(
					OntologyTermQueryRepository.ENTITY_TYPE, OntologyTermQueryRepository.TYPE_ONTOLOGYTERM),
					entityMetaDataIndexedOntologyTerm);
			return Iterables.size(listOfOntologyTerms) == 0;
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.ROOT))
		{
			return true;
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			List<OntologyTermEntity> refEntities = new ArrayList<OntologyTermEntity>();
			for (OntologyTerm ontologyTerm : ontologyService.getRootOntologyTerms(entity
					.getString(OntologyQueryRepository.ONTOLOGY_IRI)))
			{
				if (ontologyTerm instanceof OntologyTermEntity)
				{
					refEntities.add((OntologyTermEntity) ontologyTerm);
				}
			}
			return refEntities;
		}
		return entity.get(attributeName);
	}

	@Override
	public String getIri()
	{
		return getValueInternal(OntologyQueryRepository.ONTOLOGY_IRI);
	}

	@Override
	public String getLabel()
	{
		return getValueInternal(OntologyQueryRepository.ONTOLOGY_NAME);
	}

	@Override
	public String getDescription()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public String getVersion()
	{
		return StringUtils.EMPTY;
	}
}
