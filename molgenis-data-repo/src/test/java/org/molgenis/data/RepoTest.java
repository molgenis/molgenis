package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.testng.annotations.Test;

public class RepoTest
{
	@Test
	public void TestIterator()
	{	
		Repo r = mock(Repo.class);
		when(r.iterator()).thenReturn((Arrays.asList((Entity[])new Person[]{new Person()}).iterator()));
		when(r.iterator(Person.class)).thenReturn(Arrays.asList(new Person[]{new Person()}));
		
		for (Entity e : r)
		{
			System.out.println("found generic: " + e.getClass());
		}
		
		for(Person p: r.iterator(Person.class))
		{
			System.out.println("found specific: " + p.getClass());
		}
	}

}
