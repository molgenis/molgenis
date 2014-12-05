package org.molgenis.ontology.beans;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.OntologyQueryRepository;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

public class OntologyEntity extends AbstractSemanticEntity
{
	private static final long serialVersionUID = 1L;
	private final DataService dataService;
	private final OntologyService ontologyService;

	public OntologyEntity(Entity entity, EntityMetaData entityMetaData, DataService dataService,
			SearchService searchService, OntologyService ontologyService)
	{
		super(entity, entityMetaData, searchService);
		this.dataService = dataService;
		this.ontologyService = ontologyService;
	}

	@Override
	public Object get(String attributeName)
	{
		if (attributeName.equalsIgnoreCase(OntologyQueryRepository.FIELDTYPE))
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(entity
					.getString(OntologyQueryRepository.ONTOLOGY_NAME));
			long count = searchService.count(new QueryImpl().eq(OntologyTermQueryRepository.ENTITY_TYPE,
					OntologyTermQueryRepository.TYPE_ONTOLOGYTERM), entityMetaDataIndexedOntologyTerm);
			return count == 0 ? MolgenisFieldTypes.STRING.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND
					.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.LAST))
		{
			EntityMetaData entityMetaDataIndexedOntologyTerm = dataService.getEntityMetaData(entity
					.getString(OntologyQueryRepository.ONTOLOGY_NAME));
			long count = searchService.count(new QueryImpl().eq(OntologyTermQueryRepository.ENTITY_TYPE,
					OntologyTermQueryRepository.TYPE_ONTOLOGYTERM), entityMetaDataIndexedOntologyTerm);
			return count == 0;
		}

		if (attributeName.equalsIgnoreCase(OntologyTermIndexRepository.ROOT))
		{
			return true;
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			return ontologyService.getRootOntologyTermEntities(entity.getString(OntologyQueryRepository.ONTOLOGY_IRI));
		}
		return entity.get(attributeName);
	}
}