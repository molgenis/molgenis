package org.molgenis.charts.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChartDataServiceImplTest
{
	private ChartDataServiceImpl chartDataService;
	private DataService dataServiceMock;

	@BeforeMethod
	public void beforeMethod()
	{
		dataServiceMock = mock(DataService.class);
		chartDataService = new ChartDataServiceImpl(dataServiceMock);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void getDataMatrix()
	{
		String entityName = "entity";
		List<Entity> entities = new ArrayList<Entity>();

		Entity e1 = new MapEntity();
		e1.set("patient", "patient1");
		e1.set("probe", 1.5);
		entities.add(e1);

		Entity e2 = new MapEntity();
		e2.set("patient", "patient2");
		e2.set("probe", 1.6);
		entities.add(e2);

		final Repository repo = mock(Repository.class);
		when(repo.iterator()).thenReturn(entities.iterator());

		when(dataServiceMock.getRepositoryByEntityName(entityName)).thenAnswer(new Answer<Repository>()
		{
			@Override
			public Repository answer(InvocationOnMock invocation) throws Throwable
			{
				return repo;
			}
		});

		DataMatrix matrix = chartDataService.getDataMatrix(entityName, Arrays.asList("probe"), "patient",
				Collections.<QueryRule> emptyList());

		assertNotNull(matrix);
		assertEquals(matrix.getColumnTargets(), Arrays.asList(new Target("probe")));
		assertEquals(matrix.getRowTargets(), Arrays.asList(new Target("patient1"), new Target("patient2")));
		assertEquals(matrix.getValues(), Arrays.asList(Arrays.asList(1.5), Arrays.asList(1.6)));
	}
}
