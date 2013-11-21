package org.molgenis.omx.biobankconnect.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermTable extends AbstractFilterableTupleTable
{

	private final OntologyLoader loader;
	private final String ontologyIRI;
	private final String ontologyName;
	private final static String NODE_PATH = "nodePath";
	private final static String BOOST = "boost";
	private final static String ONTOLOGY_IRI = "ontologyIRI";
	private final static String ONTOLOGY_NAME = "ontologyName";
	private final static String ONTOLOGY_TERM = "ontologyTerm";
	private final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private final static String SYNONYMS = "ontologyTermSynonym";
	private final static String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	private final static String ONTOLOGY_LABEL = "ontologyLabel";
	private final static String ENTITY_TYPE = "entity_type";

	public OntologyTermTable(OntologyLoader loader)
	{
		this.loader = loader;
		this.ontologyName = this.loader.getOntologyName();
		this.ontologyIRI = this.loader.getOntologyIRI();
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();
		createOntologyTable(tuples, loader);
		return tuples.iterator();
	}

	private void createOntologyTable(List<Tuple> tuples, OntologyLoader model)
	{
		int count = 0;
		for (OWLClass subClass : model.getTopClasses())
		{
			recursiveAddTuple("0." + count, subClass, model, tuples);
			count++;
		}
	}

	private void recursiveAddTuple(String termPath, OWLClass cls, OntologyLoader ontologyLoader, List<Tuple> tuples)
	{
		String label = ontologyLoader.getLabel(cls).replaceAll("[^a-zA-Z0-9 ]", " ");
		Set<String> synonyms = new HashSet<String>();
		synonyms.add(label);
		synonyms.addAll(ontologyLoader.getSynonyms(cls));
		// listOfChildren.addAll(model.getAssociatedClasses(cls));
		StringBuilder alternativeDefinitions = new StringBuilder();
		for (Set<OWLClass> alternativeDefinition : ontologyLoader.getAssociatedClasses(cls))
		{
			StringBuilder newDefinition = new StringBuilder();
			for (OWLClass associatedClass : alternativeDefinition)
			{
				if (newDefinition.length() != 0) newDefinition.append(',');
				newDefinition.append(associatedClass.getIRI().toString());
			}
			if (alternativeDefinitions.length() != 0 && newDefinition.length() != 0) alternativeDefinitions
					.append("&&&");
			alternativeDefinitions.append(newDefinition);
		}
		if (alternativeDefinitions.length() != 0)
		{
			System.out.println(alternativeDefinitions.toString());
		}
		for (String synonym : synonyms)
		{
			KeyValueTuple tuple = new KeyValueTuple();
			tuple.set(NODE_PATH, termPath);
			tuple.set(BOOST, false);
			tuple.set(ONTOLOGY_IRI, ontologyIRI);
			tuple.set(ONTOLOGY_NAME, ontologyName);
			tuple.set(ONTOLOGY_TERM, label);
			tuple.set(ONTOLOGY_TERM_IRI, cls.getIRI().toString());
			tuple.set(ONTOLOGY_LABEL, ontologyLoader.getOntologyName());
			tuple.set(ENTITY_TYPE, "ontologyTerm");
			tuple.set(SYNONYMS, synonym.replaceAll("[^a-zA-Z0-9 ]", " "));
			tuple.set(ALTERNATIVE_DEFINITION, alternativeDefinitions.toString());
			tuples.add(tuple);
		}

		Set<OWLClass> listOfChildren = ontologyLoader.getChildClass(cls);
		if (listOfChildren.size() > 0)
		{
			int i = 0;
			for (OWLClass childClass : listOfChildren)
			{
				String childTermPath = termPath + "." + i;
				recursiveAddTuple(childTermPath, childClass, ontologyLoader, tuples);
				i++;
			}
		}
	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		List<Field> columns = new ArrayList<Field>();
		columns.add(new Field(NODE_PATH));
		columns.add(new Field(BOOST));
		columns.add(new Field(ONTOLOGY_IRI));
		columns.add(new Field(ONTOLOGY_TERM));
		columns.add(new Field(ONTOLOGY_TERM_IRI));
		columns.add(new Field(ONTOLOGY_LABEL));
		columns.add(new Field(SYNONYMS));
		columns.add(new Field(ENTITY_TYPE));
		columns.add(new Field(ALTERNATIVE_DEFINITION));
		return columns;
	}

	@Override
	public int getCount()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();
		createOntologyTable(tuples, loader);
		return tuples.size();
	}
}
