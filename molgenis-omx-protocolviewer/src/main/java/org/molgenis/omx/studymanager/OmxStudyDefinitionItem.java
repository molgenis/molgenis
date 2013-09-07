package org.molgenis.omx.study;

import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.target.OntologyTerm;

public class OmxStudyDefinitionItem implements StudyDefinitionItem
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
		return ontologyTerm != null ? ontologyTerm.getOntology().getOntologyAccession() : null;
	}
}
