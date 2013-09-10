package org.molgenis.omx.studymanager;

import java.util.List;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;

public class OmxStudyDefinitionItem implements CatalogItem
{
	private final ObservableFeature observableFeature;

	public OmxStudyDefinitionItem(ObservableFeature observableFeature)
	{
		if (observableFeature == null) throw new IllegalArgumentException("observableFeature is null");
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
		List<OntologyTerm> ontologyTerm = observableFeature.getDefinition();
		return ontologyTerm != null && ontologyTerm.size() > 0 ? ontologyTerm.get(0).getTermAccession() : null;
	}

	@Override
	public String getCodeSystem()
	{
		List<OntologyTerm> ontologyTerm = observableFeature.getDefinition();
		return ontologyTerm != null && ontologyTerm.size() > 0 ? ontologyTerm.get(0).getOntology()
				.getOntologyAccession() : null;
	}
}
