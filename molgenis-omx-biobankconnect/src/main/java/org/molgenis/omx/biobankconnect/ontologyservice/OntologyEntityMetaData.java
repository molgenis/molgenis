package org.molgenis.omx.biobankconnect.ontologyservice;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.AbstractEntityMetaData;
import org.molgenis.search.Hit;
import org.molgenis.util.ApplicationContextProvider;

import com.google.common.collect.Iterables;

public class OntologyEntityMetaData extends AbstractEntityMetaData
{
	// @Autowired
	// private SearchService searchService;
	private final OntologyService ontologyService;

	private final Hit ontology;
	private final String ontologyUrl;

	private transient Iterable<AttributeMetaData> cachedAttributes;

	public OntologyEntityMetaData(Hit ontology)
	{
		if (ontology == null) throw new IllegalArgumentException("ontology is null");
		this.ontology = ontology;
		this.ontologyUrl = ontology.getColumnValueMap().get("url").toString();
		ontologyService = ApplicationContextProvider.getApplicationContext().getBean(OntologyService.class);
	}

	@Override
	public String getName()
	{
		return ontology.getColumnValueMap().get("url").toString();
	}

	@Override
	public String getLabel()
	{
		return ontology.getColumnValueMap().get("ontologyLabel").toString();
	}

	@Override
	public String getDescription()
	{
		return StringUtils.EMPTY;
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		if (cachedAttributes == null)
		{
			for (Hit hit : ontologyService.getRootOntologyTerms(ontologyUrl))
			{
				if (cachedAttributes == null) cachedAttributes = new OntologyTermEntityMetaData(hit).getAttributes();
				else cachedAttributes = Iterables.concat(cachedAttributes,
						new OntologyTermEntityMetaData(hit).getAttributes());
			}
		}
		return cachedAttributes;
	}
}
