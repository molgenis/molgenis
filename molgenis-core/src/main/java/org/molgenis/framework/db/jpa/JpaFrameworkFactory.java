package org.molgenis.framework.db.jpa;

/**
 * 
 * @author joris lops
 */
public class JpaFrameworkFactory
{
	public static JpaFramework createFramework()
	{
		return new HibernateImp();
	}
}
