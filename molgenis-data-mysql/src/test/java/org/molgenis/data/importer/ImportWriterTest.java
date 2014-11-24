package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.AppConfig;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.security.permission.PermissionSystemService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * Writer-specific tests.
 */
@ContextConfiguration(classes = AppConfig.class)
public class ImportWriterTest extends AbstractTestNGSpringContextTests
{
	private DataService dataService;
	private WritableMetaDataService metaDataService;
	private PermissionSystemService permissionSystemService;
	private RepositoryCollection source;
	private ManageableCrudRepositoryCollection target;
	private ImportWriter writer;

	@BeforeMethod
	public void beforeMethod()
	{
		dataService = mock(DataService.class);
		metaDataService = mock(WritableMetaDataService.class);
		permissionSystemService = mock(PermissionSystemService.class);
		source = mock(RepositoryCollection.class);
		target = mock(ManageableCrudRepositoryCollection.class);
		writer = new ImportWriter(dataService, metaDataService, permissionSystemService);
	}

	@Test
	public void testHappyPath()
	{
		Map<String, org.molgenis.data.Package> packages = new LinkedHashMap<String, org.molgenis.data.Package>();
		org.molgenis.data.Package p = new PackageImpl("pack", "my package");
		packages.put("pack", p);

		DefaultEntityMetaData emd = new DefaultEntityMetaData("entityName");
		emd.addAttribute("id").setNillable(false).setIdAttribute(true);
		emd.addAttribute("name");
		emd.setPackage(p);

		ParsedMetaData parsedMetaData = new ParsedMetaData(Collections.<EntityMetaData> singletonList(emd), packages);
		EmxImportJob job = new EmxImportJob(DatabaseAction.ADD, source, parsedMetaData, target);

		CrudRepository targetRepo = mock(CrudRepository.class);
		CrudRepository sourceRepo = mock(CrudRepository.class);

		when(source.getRepositoryByEntityName("entityName")).thenReturn(sourceRepo);
		when(source.getEntityNames()).thenReturn(Collections.singletonList("entityName"));
		when(target.add(emd)).thenReturn(targetRepo);
		when(target.getRepositoryByEntityName("pack_entityName")).thenReturn(targetRepo);
		when(targetRepo.getEntityMetaData()).thenReturn(emd);
		when(targetRepo.add(Mockito.argThat(new BaseMatcher<Iterable<Entity>>()
		{

			@Override
			public boolean matches(Object arg0)
			{
				Iterable<Entity> iterable = (Iterable<Entity>) arg0;
				List<Entity> entities = Lists.newArrayList(iterable);
				assertEquals(entities.size(), 1);
				assertEquals(entities.get(0).get("id"), "123");
				return true;
			}

			@Override
			public void describeTo(Description arg0)
			{
				arg0.appendText("Is iterable");
			}

		}))).thenReturn(1);

		Entity e = new MapEntity(emd);
		e.set("id", "123");
		e.set("name", "e");
		final List<Entity> entityList = Collections.singletonList(e);
		stub(sourceRepo.iterator()).toAnswer(new Answer<Iterator<Entity>>()
		{
			@Override
			public Iterator<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return entityList.iterator();
			}
		});

		writer.doImport(job);

		assertEquals(job.report.getNrImportedEntitiesMap().get("pack_entityName"), (Integer) 1);

		verify(metaDataService).addPackage(p);
		verify(target).add(emd);
		// once for the IDs, once for the update
		verify(sourceRepo, Mockito.atMost(2)).iterator();
	}
}
