package org.molgenis.omx.studymanager;

import java.util.List;

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
}
