package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.tupletable.AbstractFilterableTupleTable;
import org.molgenis.framework.tupletable.DatabaseTupleTable;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.model.elements.Field;
import org.molgenis.util.tuple.Tuple;

public class OntologyTable extends AbstractFilterableTupleTable implements DatabaseTupleTable
{

	private File ontologyFile;
	private Database db;

	public OntologyTable(File ontologyFile, Database db)
	{
		this.db = db;
		this.ontologyFile = ontologyFile;
	}

	@Override
	public Iterator<Tuple> iterator()
	{
		List<Tuple> tuples = new ArrayList<Tuple>();

		return tuples.iterator();
	}

	@Override
	public Database getDb()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDb(Database db)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<Field> getAllColumns() throws TableException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCount() throws TableException
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
