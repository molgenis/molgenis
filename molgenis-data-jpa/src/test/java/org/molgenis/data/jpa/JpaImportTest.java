package org.molgenis.data.jpa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.jpa.importer.AbstractEntitiesImporter;
import org.molgenis.data.jpa.importer.EntitiesImporter;
import org.molgenis.data.jpa.importer.EntityImportService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.MolgenisValidationException;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class JpaImportTest extends BaseJpaTest
{
	private EntitiesImporter importer;

	@Override
	@BeforeMethod
	public void beforeMethod()
	{
		super.beforeMethod();

		EntityImportService eis = new EntityImportService();
		eis.setDataService(dataService);

		importer = new AbstractEntitiesImporter(fileRepositorySourceFactory, eis)
		{
			@Override
			protected Set<String> getEntitiesImportable()
			{
				return Sets.newHashSet("person");
			}
		};
	}

	private File loadTestFile(String name) throws IOException
	{
		InputStream in = getClass().getResourceAsStream("/" + name);
		File f = File.createTempFile(name, "." + StringUtils.getFilenameExtension(name));
		FileCopyUtils.copy(in, new FileOutputStream(f));

		return f;
	}

	@Test
	public void testOk() throws IOException
	{
		importer.importEntities(loadTestFile("ok.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
		assertEquals(repo.count(), 2);
	}

	@Test
	public void testMissingRequiredValue() throws IOException
	{
		try
		{
			importer.importEntities(loadTestFile("missing-lastname.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "lastName");
			assertEquals(e.getViolations().iterator().next().getRownr(), Long.valueOf(2));
		}
	}

	@Test
	public void testWrongIntValue() throws IOException
	{
		try
		{
			importer.importEntities(loadTestFile("wrong-int-type.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "age");
		}
	}

	@Test
	public void testXref() throws IOException
	{
		importer.importEntities(loadTestFile("xref.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
		assertEquals(repo.count(), 2);

		Person piet = dataService.findOne("Person", new QueryImpl().eq("firstName", "Piet"), Person.class);
		assertNotNull(piet);
		assertNull(piet.getFather());

		Person hans = dataService.findOne("Person", new QueryImpl().eq("firstName", "Hans"), Person.class);
		assertNotNull(hans);
		assertNotNull(hans.getFather());
		assertEquals(hans.getFather().getFirstName(), "Piet");
	}

	@Test
	public void testXrefCircular() throws IOException
	{
		try
		{
			importer.importEntities(loadTestFile("xref-circular.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 2);
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "father");
			assertEquals(e.getViolations().iterator().next().getRownr(), Long.valueOf(1));
		}
	}

	@Test
	public void testMref() throws IOException
	{
		importer.importEntities(loadTestFile("mref.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
		assertEquals(repo.count(), 3);

		Person piet = dataService.findOne("Person", new QueryImpl().eq("firstName", "Piet"), Person.class);
		assertNotNull(piet);
		assertEquals(piet.getChildren().size(), 2);

		Person hans = dataService.findOne("Person", new QueryImpl().eq("firstName", "Hans"), Person.class);
		assertNotNull(hans);
		assertEquals(hans.getChildren().size(), 1);

		Person mark = dataService.findOne("Person", new QueryImpl().eq("firstName", "Mark"), Person.class);
		assertNotNull(mark);
		assertTrue(mark.getChildren().isEmpty());
	}

	@Test
	public void testMrefWrong() throws IOException
	{
		try
		{
			importer.importEntities(loadTestFile("mref-wrong.xlsx"), DatabaseAction.ADD_UPDATE_EXISTING);
			fail("Should have thrown MolgenisValidationException");
		}
		catch (MolgenisValidationException e)
		{
			assertEquals(e.getViolations().size(), 1);
			assertEquals(e.getViolations().iterator().next().getViolatedAttribute().getName(), "children");
			assertEquals(e.getViolations().iterator().next().getRownr(), Long.valueOf(3));
		}
	}
}
