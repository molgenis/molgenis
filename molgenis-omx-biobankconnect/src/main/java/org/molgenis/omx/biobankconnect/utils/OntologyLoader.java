package org.molgenis.omx.biobankconnect.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OntologyLoader
{
	private String ontologyIRI = null;
	private String ontologyName = null;
	private File ontologyFile = null;
	private OWLDataFactory factory = null;
	private OWLOntology ontology = null;
	private OWLOntologyManager manager = null;
	private Set<String> synonymsProperties;
	{
		synonymsProperties = new HashSet<String>(Arrays.asList(
				"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN",
				"http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#P90"));
	}
	private Set<String> owlObjectProperties;
	{
		owlObjectProperties = new HashSet<String>(
				Arrays.asList("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#is_associated_with"));
	}
	private Map<String, OWLClass> hashToRetrieveClass = new HashMap<String, OWLClass>();

	public OntologyLoader(OWLOntologyManager manager, OWLDataFactory factory)
	{
		this.manager = manager;
		this.factory = factory;
	}

	public OntologyLoader(String ontologyName, File ontologyFile) throws OWLOntologyCreationException
	{
		this.ontologyFile = ontologyFile;
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.ontologyName = ontologyName;
		this.ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
		this.ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
	}

	public void loadOntology(String ontologyName, File ontologyFile) throws OWLOntologyCreationException
	{
		this.ontologyName = ontologyName;
		this.ontologyFile = ontologyFile;
		this.ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
		this.ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
	}

	public void preProcessing()
	{
		for (OWLClass cls : ontology.getClassesInSignature())
		{
			hashToRetrieveClass.put(getLabel(cls).trim().toLowerCase(), cls);
		}
	}

	public Set<OWLAnnotationAssertionAxiom> getAllAnnotationAxiom(OWLClass cls)
	{
		Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<OWLAnnotationAssertionAxiom>();
		for (OWLAnnotation annotation : cls.getAnnotations(ontology))
		{
			axioms.add(factory.getOWLAnnotationAssertionAxiom(cls.getIRI(), annotation));
		}
		return axioms;
	}

	public Set<OWLClass> getTopClasses()
	{
		Set<OWLClass> listOfTopClasses = new HashSet<OWLClass>();
		for (OWLClass cls : ontology.getClassesInSignature())
		{
			if (ontology.getSubClassAxiomsForSubClass(cls).size() == 0
					&& ontology.getEquivalentClassesAxioms(cls).size() == 0) listOfTopClasses.add(cls);
		}
		return listOfTopClasses;
	}

	public List<Set<OWLClass>> getAssociatedClasses(OWLClass cls)
	{
		List<Set<OWLClass>> alternativeDefinitions = new ArrayList<Set<OWLClass>>();
		for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(cls))
		{
			Set<OWLClass> associatedTerms = new HashSet<OWLClass>();
			OWLClassExpression expression = axiom.getSuperClass();
			if (expression.isAnonymous())
			{
				for (OWLObjectProperty property : expression.getObjectPropertiesInSignature())
				{
					if (owlObjectProperties.contains(property.getIRI().toString()))
					{
						for (OWLClass associatedClass : expression.getClassesInSignature())
						{
							associatedTerms.add(associatedClass);
						}
					}
				}
			}
			alternativeDefinitions.add(associatedTerms);
		}
		return alternativeDefinitions;
	}

	public Set<String> getSynonyms(OWLClass cls)
	{
		Set<String> listOfSynonyms = new HashSet<String>();
		for (String eachSynonymProperty : synonymsProperties)
		{
			OWLAnnotationProperty property = factory.getOWLAnnotationProperty(IRI.create(eachSynonymProperty));
			for (OWLAnnotation annotation : cls.getAnnotations(ontology, property))
			{
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				listOfSynonyms.add(val.getLiteral());
			}
		}
		return listOfSynonyms;
	}

	public Set<OWLClass> getChildClass(OWLClass cls)
	{
		Set<OWLClass> listOfClasses = new HashSet<OWLClass>();
		for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSuperClass(cls))
		{
			OWLClassExpression expression = axiom.getSubClass();
			if (!expression.isAnonymous())
			{
				listOfClasses.add(expression.asOWLClass());
			}
		}
		return listOfClasses;
	}

	public String getLabel(OWLEntity cls)
	{
		String labelValue = "";
		try
		{
			OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			for (OWLAnnotation annotation : cls.getAnnotations(ontology, label))
			{
				if (annotation.getValue() instanceof OWLLiteral)
				{
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					labelValue = val.getLiteral().toString();
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed to get label for OWLClass " + cls);
		}
		return labelValue;
	}

	public String getOntologyLabel()
	{
		OWLAnnotationProperty labelProperty = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
		String ontologyLabel = StringUtils.EMPTY;
		for (OWLAnnotation annotation : ontology.getAnnotations())
		{
			if (annotation.getProperty().equals(labelProperty) && annotation.getValue() instanceof OWLLiteral)
			{
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				ontologyLabel = val.getLiteral();
			}
		}
		return ontologyLabel;
	}

	public String getOntologyIRI()
	{
		return ontologyIRI;
	}

	public String getOntologyName()
	{
		return ontologyName;
	}

	public String getOntologyFilePath()
	{
		return ontologyFile.getAbsolutePath();
	}

	public Map<String, OWLClass> getHashToRetrieveClass()
	{
		return hashToRetrieveClass;
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass cls)
	{
		return ontology.getSubClassAxiomsForSuperClass(cls);
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls)
	{
		return ontology.getSubClassAxiomsForSubClass(cls);
	}
}