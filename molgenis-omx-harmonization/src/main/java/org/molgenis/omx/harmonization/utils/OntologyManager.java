package org.molgenis.omx.harmonization.utils;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class OntologyManager
{
	private OWLDataFactory factory = null;
	private OWLOntologyManager manager = null;
	private OntologyLoader ontologyLoader = null;
	private OntologyCreator ontologyCreator = null;

	public OntologyManager()
	{
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		ontologyLoader = new OntologyLoader(manager, factory);
		ontologyCreator = new OntologyCreator(manager, factory, ontologyLoader);
	}

	public void loadExistingOntology(File ontologyFile) throws OWLOntologyCreationException
	{
		ontologyLoader.loadOntology(null, ontologyFile);
		ontologyLoader.preProcessing();
	}

	public void copyOntologyContent(Set<String> terms) throws OWLOntologyCreationException
	{
		if (terms != null)
		{
			ontologyCreator.createOntology(ontologyLoader.getOntologyIRI() + ".copy");
			ontologyCreator.parseOntologyTerms(terms, ontologyLoader);
		}
	}

	public void saveOntology(File file) throws OWLOntologyStorageException
	{
		ontologyCreator.saveOntology(file);
	}
}