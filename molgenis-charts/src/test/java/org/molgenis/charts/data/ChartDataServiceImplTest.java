package org.molgenis.charts.data;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ChartDataServiceImplTest extends AbstractMolgenisSpringTest
{
	private ChartDataServiceImpl chartDataService;
	private DataService dataServiceMock;

	@Autowired
	private EntityMetaDataFactory entityMetaFactory;
	@Autowired
	private AttributeMetaDataFactory attrMetaFactory;

	@BeforeMethod
	public void beforeMethod()
	{
		dataServiceMock = mock(DataService.class);
		chartDataService = new ChartDataServiceImpl(dataServiceMock);
	}

	@Test
	public void getDataMatrix()
	{
		String entityName = "entity";
		List<Entity> entities = new ArrayList<Entity>();

		Attribute patientAttr = attrMetaFactory.create().setName("patient");
		Attribute probeAttr = attrMetaFactory.create().setName("probe").setDataType(DECIMAL);
		EntityMetaData entityMetaData = entityMetaFactory.create();
		entityMetaData.addAttributes(newArrayList(patientAttr, probeAttr));

		Entity e1 = new DynamicEntity(entityMetaData);
		e1.set("patient", "patient1");
		e1.set("probe", 1.5);
		entities.add(e1);

		Entity e2 = new DynamicEntity(entityMetaData);
		e2.set("patient", "patient2");
		e2.set("probe", 1.6);
		entities.add(e2);

		final Repository<Entity> repo = mock(Repository.class);
		when(repo.iterator()).thenReturn(entities.iterator());

		when(dataServiceMock.getRepository(entityName)).thenAnswer(new Answer<Repository<Entity>>()
		{
			@Override
			public Repository<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return repo;
			}
		});

		DataMatrix matrix = chartDataService
				.getDataMatrix(entityName, Arrays.asList("probe"), "patient", Collections.<QueryRule>emptyList());

		assertNotNull(matrix);
		assertEquals(matrix.getColumnTargets(), Arrays.asList(new Target("probe")));
		assertEquals(matrix.getRowTargets(), Arrays.asList(new Target("patient1"), new Target("patient2")));
		assertEquals(matrix.getValues(), Arrays.asList(Arrays.asList(1.5), Arrays.asList(1.6)));
	}
}
