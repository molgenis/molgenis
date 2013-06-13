package org.molgenis.omx.harmonizationIndexer.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class OntologyModel
{
	private String ontologyIRI = null;
	private OWLDataFactory factory = null;
	private OWLOntology ontology = null;
	private OWLOntologyManager manager = null;
	private HashMap<String, OWLClass> labelToClass = new HashMap<String, OWLClass>();
	private Set<String> synonymsProperties;
	{
		synonymsProperties = new HashSet<String>(
				Arrays.asList("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#FULL_SYN"));
	}

	public OntologyModel(File ontologyFile) throws OWLOntologyCreationException
	{
		this.manager = OWLManager.createOWLOntologyManager();
		this.factory = manager.getOWLDataFactory();
		this.ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
		this.ontologyIRI = ontology.getOntologyID().getOntologyIRI().toString();
	}

	public void labelMapURI()
	{
		for (OWLClass cls : ontology.getClassesInSignature())
		{
			OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			String labelString = "";
			for (OWLAnnotation annotation : cls.getAnnotations(ontology, label))
			{
				if (annotation.getValue() instanceof OWLLiteral)
				{
					OWLLiteral val = (OWLLiteral) annotation.getValue();
					labelString = val.getLiteral().toLowerCase();
					labelToClass.put(labelString.toLowerCase(), cls);
				}
			}
		}
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

	public List<String> getSynonyms(OWLClass cls)
	{
		List<String> listOfSynonyms = new ArrayList<String>();
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

	public Set<OWLClass> getParentClass(OWLClass cls)
	{
		Set<OWLClass> listOfClasses = new HashSet<OWLClass>();
		for (OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(cls))
		{
			OWLClassExpression expression = axiom.getSuperClass();
			if (!expression.isAnonymous())
			{
				listOfClasses.add(expression.asOWLClass());
			}
		}
		return listOfClasses;
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
			System.out.println("The annotation is null!");
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

	public OWLOntology getOntology()
	{
		return this.ontology;
	}

	public HashMap<String, OWLClass> getLabelToClass()
	{
		return labelToClass;
	}

	public String getOntologyTermID(String ontologyTerm)
	{
		OWLClass cls = labelToClass.get(ontologyTerm.toLowerCase());
		String owlClassID = cls.getIRI().toString().substring(ontologyIRI.length() - 2);
		return owlClassID;
	}

	public String getOntologyIRI()
	{
		return ontologyIRI;
	}

	public OWLDataFactory getFactory()
	{
		return factory;
	}
}