package org.molgenis.charts.data;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
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
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ChartDataServiceImplTest extends AbstractMolgenisSpringTest
{
	private ChartDataServiceImpl chartDataService;

	@Autowired
	private DataService dataServiceMock;

	@Autowired
	private EntityTypeFactory entityTypeFactory;
	@Autowired
	private AttributeFactory attrMetaFactory;

	@BeforeMethod
	public void beforeMethod()
	{
		chartDataService = new ChartDataServiceImpl(dataServiceMock);
	}

	@Test
	public void getDataMatrix()
	{
		String entityTypeId = "entity";
		List<Entity> entities = new ArrayList<>();

		Attribute patientAttr = attrMetaFactory.create().setName("patient");
		Attribute probeAttr = attrMetaFactory.create().setName("probe").setDataType(DECIMAL);
		EntityType entityType = entityTypeFactory.create();
		entityType.addAttributes(newArrayList(patientAttr, probeAttr));

		Entity e1 = new DynamicEntity(entityType);
		e1.set("patient", "patient1");
		e1.set("probe", 1.5);
		entities.add(e1);

		Entity e2 = new DynamicEntity(entityType);
		e2.set("patient", "patient2");
		e2.set("probe", 1.6);
		entities.add(e2);

		@SuppressWarnings("unchecked")
		final Repository<Entity> repo = mock(Repository.class);
		when(repo.iterator()).thenReturn(entities.iterator());

		when(dataServiceMock.getRepository(entityTypeId)).thenAnswer(new Answer<Repository<Entity>>()
		{
			@Override
			public Repository<Entity> answer(InvocationOnMock invocation) throws Throwable
			{
				return repo;
			}
		});

		DataMatrix matrix = chartDataService
				.getDataMatrix(entityTypeId, Arrays.asList("probe"), "patient", Collections.<QueryRule>emptyList());

		assertNotNull(matrix);
		assertEquals(matrix.getColumnTargets(), Arrays.asList(new Target("probe")));
		assertEquals(matrix.getRowTargets(), Arrays.asList(new Target("patient1"), new Target("patient2")));
		assertEquals(matrix.getValues(), Arrays.asList(Arrays.asList(1.5), Arrays.asList(1.6)));
	}
}
