package org.molgenis.omx.biobankconnect.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;

public class OntologyTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{
	private Database db;
	private OntologyLoader loader;
	private final String ONTOLOGY_URL = "url";
	private final String ENTITY_TYPE = "entity_type";
	private final String ONTOLOGY_LABEL = "ontologyLabel";

	public OntologyTable(OntologyLoader loader, Database db)
	{
		this.loader = loader;
		setDb(db);
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();

		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(ONTOLOGY_URL, loader.getOntologyIRI());
		tuple.set(ONTOLOGY_LABEL, loader.getOntologyName());
		tuple.set(ENTITY_TYPE, "indexedOntology");
		tuples.add(tuple);

		return tuples.iterator();
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
		columns.add(new Field(ONTOLOGY_URL));
		columns.add(new Field(ONTOLOGY_LABEL));
		columns.add(new Field(ENTITY_TYPE));
		return columns;
	}

	@Override
	public int getCount()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();

		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(ONTOLOGY_URL, loader.getOntologyIRI());
		tuple.set(ONTOLOGY_LABEL, loader.getOntologyLabel());
		tuple.set(ENTITY_TYPE, "indexedOntology");
		tuples.add(tuple);

		return tuples.size();
	}
}
