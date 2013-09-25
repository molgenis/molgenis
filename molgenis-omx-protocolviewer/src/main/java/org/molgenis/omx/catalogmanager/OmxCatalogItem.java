package org.molgenis.omx.catalogmanager;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;

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
		return observableFeature.getIdentifier();
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
		OntologyTerm ontologyTerm = observableFeature.getDefinition();
		return ontologyTerm != null ? ontologyTerm.getTermAccession() : null;
	}

	@Override
	public String getCodeSystem()
	{
		OntologyTerm ontologyTerm = observableFeature.getDefinition();
		return ontologyTerm != null ? ontologyTerm.getOntology().getOntologyAccession() : null;
	}
}
