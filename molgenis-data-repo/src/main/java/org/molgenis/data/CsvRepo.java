package org.molgenis.data;

import java.io.File;
import java.io.FileNotFoundException;

import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.AbstractRepo;

public class CsvRepo extends AbstractRepo implements Repo
{
	public CsvRepo(File f) throws FileNotFoundException
	{
		this.repository = new CsvRepository(f);
	}
}
