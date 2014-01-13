package org.molgenis.data;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;


public class JpaRepoTest
{
	@Test
	public void test() throws IOException
	{	
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("molgenis");
		EntityManager em = emf.createEntityManager();
		if(!em.getTransaction().isActive())
	        em.getTransaction().begin(); 
		
		JpaRepo r = new JpaRepo(em, Person.class);
		
		//empty to repo
		r.deleteAll();
		
		for(int i = 1; i<=10; i++)
		{
			Person p = new Person();
			p.setId(i);
			p.setFirstName("John"+i);
			p.setLastName("Doe"+i);
			r.add(p);
		}
		
		if(!em.getTransaction().isActive())
	        em.getTransaction().commit();

		//generic
		System.out.println("generic iteration:");
		for (Entity e : r)
		{
			System.out.println(e);

		}
		
		//specific
		System.out.println("type-specific iteration:");
		for(Person e: r.iterator(Person.class))
		{
			System.out.println(e);
			
		}

	}

}
