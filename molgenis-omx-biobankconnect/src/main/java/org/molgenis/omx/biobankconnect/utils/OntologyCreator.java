package org.molgenis.omx.biobankconnect.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OntologyCreator
{
	private OWLOntology ontology = null;
	private OWLOntologyManager manager = null;
	private OWLDataFactory factory = null;
	private Set<String> termsFound = null;
	private Set<String> termsNotFound = null;

	public OntologyCreator(String prefix) throws OWLOntologyCreationException
	{
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.createOntology(prefix);
	}

	public OntologyCreator(OWLOntologyManager manager, OWLDataFactory factory, OntologyLoader ontologyLoader)
	{
		this.manager = manager;
		this.factory = factory;
		this.termsFound = new HashSet<String>();
		this.termsNotFound = new HashSet<String>();
	}

	public void addClass(OWLClass cls, OWLClass parentClass)
	{
		if (parentClass == null) parentClass = factory.getOWLThing();
		manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(cls, parentClass)));
	}

	public void addSynonym(OWLClass cls, String synonym)
	{
		OWLAnnotationProperty property = factory.getOWLAnnotationProperty(IRI
				.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN"));
		OWLAnnotation commentAnno = factory.getOWLAnnotation(property, factory.getOWLLiteral(synonym, "en"));
		OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), commentAnno);
		manager.applyChange(new AddAxiom(ontology, ax));
	}

	public void addLabel(OWLClass cls, String label)
	{
		OWLAnnotationProperty property = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		OWLAnnotation commentAnno = factory.getOWLAnnotation(property, factory.getOWLLiteral(label, "en"));
		OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), commentAnno);
		manager.applyChange(new AddAxiom(ontology, ax));

	}

	public OWLClass createOWLClass(String URI)
	{
		OWLClass cls = factory.getOWLClass(IRI.create(URI));
		return cls;
	}

	public OWLAnnotation createLabelAnnotation(String label)
	{
		return createAnnotation(label, OWLRDFVocabulary.RDFS_LABEL.toString());
	}

	public OWLAnnotation createDefinitionAnnotation(String definition)
	{
		return createAnnotation(definition, OWLRDFVocabulary.RDFS_COMMENT.toString());
	}

	public OWLAnnotation createAnnotation(String text, String property)
	{
		OWLAnnotationProperty annotationProperty = factory.getOWLAnnotationProperty(IRI.create(property));
		OWLLiteral value = factory.getOWLLiteral(text, "en");
		return factory.getOWLAnnotation(annotationProperty, value);
	}

	public void addAnnotations(OWLClass cls, Set<OWLAnnotation> annotations)
	{
		List<AddAxiom> changes = new ArrayList<AddAxiom>();
		for (OWLAnnotation annotation : annotations)
		{
			changes.add(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), annotation)));
		}
		manager.applyChanges(changes);
	}

	public void createOntology(String ontologyIRI) throws OWLOntologyCreationException
	{
		ontology = manager.createOntology(IRI.create(ontologyIRI));
	}

	public void saveOntology(File file) throws OWLOntologyStorageException
	{
		manager.saveOntology(ontology, IRI.create(file));
	}

	public void copyClass(OWLClass cls, OWLClass parentClass, OntologyLoader ontologyLoader)
	{
		manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(cls, parentClass)));
		List<AddAxiom> changes = new ArrayList<AddAxiom>();
		for (OWLAnnotationAssertionAxiom axiom : ontologyLoader.getAllAnnotationAxiom(cls))
		{
			changes.add(new AddAxiom(ontology, axiom));
		}
		manager.applyChanges(changes);
	}

	public void copySubclass(OWLClass cls, OntologyLoader ontologyLoader)
	{
		List<AddAxiom> changes = new ArrayList<AddAxiom>();
		for (OWLSubClassOfAxiom axiom : ontologyLoader.getSubClassAxiomsForSuperClass(cls))
		{
			for (OWLClass subClass : axiom.getClassesInSignature())
			{
				if (subClass != cls && !subClass.isAnonymous())
				{
					copyClass(subClass, cls, ontologyLoader);
					copySubclass(subClass, ontologyLoader);
				}
			}
			changes.add(new AddAxiom(ontology, axiom));
		}
		manager.applyChanges(changes);
	}

	private void copySuperClasse(OWLClass cls, OntologyLoader ontologyLoader)
	{
		List<AddAxiom> changes = new ArrayList<AddAxiom>();
		for (OWLSubClassOfAxiom axiom : ontologyLoader.getSubClassAxiomsForSubClass(cls))
		{
			OWLClassExpression expression = axiom.getSuperClass();
			if (!expression.isAnonymous())
			{
				copyClass(cls, expression.asOWLClass(), ontologyLoader);
				copySuperClasse(expression.asOWLClass(), ontologyLoader);
				changes.add(new AddAxiom(ontology, axiom));
			}
		}
		if (changes.size() == 0) copyClass(cls, factory.getOWLThing(), ontologyLoader);
		else manager.applyChanges(changes);
	}

	public void parseOntologyTerms(Set<String> terms, OntologyLoader ontologyLoader)
	{
		Map<String, OWLClass> hashRetrieveClass = ontologyLoader.getHashToRetrieveClass();
		for (String term : terms)
		{
			term = term.trim().toLowerCase();
			if (hashRetrieveClass.containsKey(term))
			{
				OWLClass cls = hashRetrieveClass.get(term);
				copySubclass(cls, ontologyLoader);
				copySuperClasse(cls, ontologyLoader);
				termsFound.add(term);
			}
			else
			{
				termsNotFound.add(term);
			}
		}
	}
}