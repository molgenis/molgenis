package org.molgenis.omx.studymanager;

import org.molgenis.catalog.CatalogItem;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.Ontology;
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
		OntologyTerm ontologyTerm = observableFeature.getDefinition();
		return ontologyTerm != null ? ontologyTerm.getTermAccession() : null;
	}

	@Override
	public String getCodeSystem()
	{
		OntologyTerm ontologyTerm = observableFeature.getDefinition();
		if (ontologyTerm == null) return null;
		Ontology ontology = ontologyTerm.getOntology();
		return ontology != null ? ontology.getOntologyAccession() : null;
	}
}
