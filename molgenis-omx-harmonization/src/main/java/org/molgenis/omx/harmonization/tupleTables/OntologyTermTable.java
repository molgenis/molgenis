package org.molgenis.omx.harmonization.tupleTables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.omx.harmonization.utils.OntologyLoader;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{

	private OntologyLoader loader;
	private Database db;
	private final String ontologyIRI;
	private final String NODE_PATH = "nodePath";
	private final String BOOST = "boost";
	private final String ONTOLOGY_IRI = "ontologyIRI";
	private final String ONTOLOGY_TERM = "ontologyTerm";
	private final String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	private final String SYNONYMS = "ontologyTermSynonym";
	private final String ONTOLOGY_LABEL = "ontologyLabel";
	private final String ENTITY_TYPE = "entity_type";

	public OntologyTermTable(OntologyLoader loader, Database db)
	{
		this.loader = loader;
		this.ontologyIRI = this.loader.getOntologyIRI();
		setDb(db);
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

	private void recursiveAddTuple(String termPath, OWLClass cls, OntologyLoader model, List<Tuple> tuples)
	{
		String label = model.getLabel(cls).replaceAll("[^a-zA-Z0-9 ]", " ");
		Set<String> synonyms = new HashSet<String>();
		synonyms.add(label);
		synonyms.addAll(model.getSynonyms(cls));
		for (String synonym : synonyms)
		{
			KeyValueTuple tuple = new KeyValueTuple();
			tuple.set(NODE_PATH, termPath);
			tuple.set(BOOST, false);
			tuple.set(ONTOLOGY_IRI, ontologyIRI);
			tuple.set(ONTOLOGY_TERM, label);
			tuple.set(ONTOLOGY_TERM_IRI, cls.getIRI().toString());
			tuple.set(ONTOLOGY_LABEL, model.getOntologyName());
			tuple.set(ENTITY_TYPE, "ontologyTerm");
			tuple.set(SYNONYMS, synonym.replaceAll("[^a-zA-Z0-9 ]", " "));
			tuples.add(tuple);
		}

		Set<OWLClass> listOfChildren = model.getChildClass(cls);
		listOfChildren.addAll(model.getAssociatedClasses(cls));
		if (listOfChildren.size() > 0)
		{
			int i = 0;
			for (OWLClass childClass : listOfChildren)
			{
				String childTermPath = termPath + "." + i;
				recursiveAddTuple(childTermPath, childClass, model, tuples);
				i++;
			}
		}
	}

	@Override
	public Database getDb()
	{
		return db;
	}

	@Override
	public void setDb(Database db)
	{
		this.db = db;
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
