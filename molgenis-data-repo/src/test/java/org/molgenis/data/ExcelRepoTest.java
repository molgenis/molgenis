package org.molgenis.data;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;


public class ExcelRepoTest
{
	@Test
	public void test() throws IOException
	{

		Repo r = new ExcelRepo(new File("src/test/resources/person.xlsx"), "person");

		//generic
		for (Entity e : r)
		{
			System.out.println(e);

		}
		
		//specific
		for(Person e: r.iterator(Person.class))
		{
			System.out.println(e);
			
		}

	}

}
