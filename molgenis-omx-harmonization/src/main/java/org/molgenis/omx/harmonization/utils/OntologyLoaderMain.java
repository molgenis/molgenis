package org.molgenis.omx.harmonization.utils;

import java.io.File;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.ebi.ontocat.OntologyServiceException;

public class OntologyLoaderMain
{
	public static void main(String[] args) throws OWLOntologyCreationException, OntologyServiceException
	{
		System.out.println("start loading");
		OntologyLoader loader = new OntologyLoader("MESH", new File(
				"/Users/chaopang/Desktop/Ontologies/NCITNCBO_v13.07e.owl"));
		System.out.println("finish loading");

		for (OWLClass cls : loader.getTopClasses())
		{
			loader.getSynonyms(cls);
		}
	}
}
