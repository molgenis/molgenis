package org.molgenis.omx.catalogmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.omx.observ.target.OntologyTerm;

import com.google.common.collect.Lists;

public class OmxCatalogItem implements CatalogItem
{
	private final ObservableFeature observableFeature;

	public OmxCatalogItem(ObservableFeature observableFeature)
	{
		if (observableFeature == null) throw new IllegalArgumentException("Observable feature is null");
		this.observableFeature = observableFeature;
	}

	@Override
	public String getId()
	{
		return observableFeature.getId().toString();
	}

	@Override
	public String getName()
	{
		return observableFeature.getName();
	}

	@Override
	public String getDescription()
	{
		return observableFeature.getDescription();
	}

	@Override
	public String getCode()
	{
		List<OntologyTerm> ontologyTerm = observableFeature.getDefinitions();
		if (ontologyTerm == null || ontologyTerm.isEmpty()) return null;
		else if (ontologyTerm.size() > 1) throw new RuntimeException("Multiple ontology terms are not supported");
		else return ontologyTerm.get(0).getTermAccession();
	}

	@Override
	public String getCodeSystem()
	{
		List<OntologyTerm> ontologyTerm = observableFeature.getDefinitions();
		if (ontologyTerm == null || ontologyTerm.isEmpty()) return null;
		else if (ontologyTerm.size() > 1) throw new RuntimeException("Multiple ontology terms are not supported");
		else
		{
			Ontology ontology = ontologyTerm.get(0).getOntology();
			return ontology != null ? ontology.getOntologyAccession() : null;
		}
	}

	@Override
	public Iterable<String> getPath()
	{
		List<String> protocolPath = new ArrayList<String>();
		Collection<Protocol> protocols = observableFeature.getFeaturesProtocolCollection();
		while (protocols != null && !protocols.isEmpty())
		{
			if (protocols.size() != 1)
			{
				throw new RuntimeException("Catalog item (group) must belong to one catalog (instead of "
						+ protocols.size() + ')');
			}
			Protocol protocol = protocols.iterator().next();
			protocolPath.add(protocol.getId().toString());
			protocols = protocol.getSubprotocolsProtocolCollection();
		}
		return Lists.reverse(protocolPath);
	}
}
