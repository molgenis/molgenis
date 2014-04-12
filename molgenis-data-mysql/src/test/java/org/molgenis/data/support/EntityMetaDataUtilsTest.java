package org.molgenis.data.support;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.excel.ExcelRepositoryCollection;
import org.molgenis.data.support.EntityMetaDataUtils;
import org.testng.annotations.Test;

/**
 * Created by mswertz on 07/04/14.
 */
public class EntityMetaDataUtilsTest
{
	@Test
	public void test() throws IOException, InvalidFormatException
	{
		BasicConfigurator.configure();
		EntityMetaDataUtils utils = new EntityMetaDataUtils();

		File f = new File(
				"/Users/mswertz/git/molgenis_selenium_2014_02_19/molgenis/molgenis-app-omx/src/test/resources/example_omx2.3.xls");
		ExcelRepositoryCollection coll = new ExcelRepositoryCollection(f);
		Collection<EntityMetaData> entities = utils.loadOMX(coll);
		for (EntityMetaData em : entities)
		{
			System.out.println(em);
            for(AttributeMetaData att: em.getAttributes())
            {
                System.out.println(att);
            }
		}

		// TODO load into new ExcelRepositoryCollection?
		//utils.copy(entities, null);

        //Collection<EntityMetaData> entities = new ExcelRepositoryCollection(f);


		System.out.println(utils.toXml(entities));
	}
}
