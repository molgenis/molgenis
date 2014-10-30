package org.molgenis.ontology.beans;

import java.math.BigDecimal;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.repository.OntologyTermQueryRepository;

import com.google.common.collect.Iterables;

public class OntologyTermEntity extends AbstractSemanticEntity
{
	private static final long serialVersionUID = 1L;
	private BigDecimal score = null;

	public OntologyTermEntity(Entity entity, EntityMetaData entityMetaData, SearchService searchService)
	{
		super(entity, entityMetaData, searchService);
	}

	@Override
	public Object get(String attributeName)
	{
		if (attributeName.equalsIgnoreCase(OntologyTermQueryRepository.FIELDTYPE))
		{
			return Boolean.parseBoolean(entity.getString(OntologyTermIndexRepository.LAST)) ? MolgenisFieldTypes.STRING
					.toString().toUpperCase() : MolgenisFieldTypes.COMPOUND.toString().toUpperCase();
		}

		if (attributeName.equalsIgnoreCase("attributes"))
		{
			if (!Boolean.parseBoolean(entity.getString(OntologyTermQueryRepository.LAST)))
			{
				String currentNodePath = entity.getString(OntologyTermQueryRepository.NODE_PATH);
				String currentOntologyTermIri = entity.getString(OntologyTermQueryRepository.ONTOLOGY_TERM_IRI);
				Query q = new QueryImpl().eq(OntologyTermIndexRepository.PARENT_NODE_PATH, currentNodePath).and()
						.eq(OntologyTermIndexRepository.PARENT_ONTOLOGY_TERM_IRI, currentOntologyTermIri)
						.pageSize(Integer.MAX_VALUE);

				return Iterables.transform(searchService.search(q, entityMetaData), new OntologyTermTransformer(
						entityMetaData, searchService));
			}
		}

		return entity.get(attributeName);
	}

	public BigDecimal getScore()
	{
		return score;
	}

	public void setScore(BigDecimal score)
	{
		this.score = score;
	}
}