package org.molgenis.data;

import java.io.File;
import java.io.FileNotFoundException;

import org.testng.annotations.Test;

public class CsvRepoTest
{
	@Test
	public void test() throws FileNotFoundException
	{

		Repo r = new CsvRepo(new File("src/test/resources/person.csv"));

		// generic
		for (Entity e : r)
		{
			System.out.println(e);

		}

		// specific
		for (Person e : r.iterator(Person.class))
		{
			System.out.println(e);

		}
	}

}
