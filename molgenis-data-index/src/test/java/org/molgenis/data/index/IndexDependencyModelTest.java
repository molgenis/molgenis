package org.molgenis.data.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class IndexDependencyModelTest
{
	@Mock
	private EntityType entity0;
	@Mock
	private EntityType entity1;
	@Mock
	private EntityType entity2;
	@Mock
	private EntityType entity3;
	@Mock
	private EntityType entity4;

	@BeforeClass
	public void beforeClass()
	{
		initMocks(this);
		when(entity0.getId()).thenReturn("0");
		when(entity1.getId()).thenReturn("1");
		when(entity2.getId()).thenReturn("2");
		when(entity3.getId()).thenReturn("3");
		when(entity4.getId()).thenReturn("4");
	}

	@Test
	public void testGetEntityTypesDependentOnDeepReferences() throws Exception
	{
		when(entity1.getIndexingDepth()).thenReturn(1);
		when(entity2.getIndexingDepth()).thenReturn(1);
		when(entity3.getIndexingDepth()).thenReturn(3);
		when(entity4.getIndexingDepth()).thenReturn(1);

		List<EntityType> entityTypes = ImmutableList.of(entity0, entity1, entity2, entity3, entity4);

		addReferences(entity0, ImmutableList.of(entity1, entity4));
		addReferences(entity1, ImmutableList.of(entity2));
		addReferences(entity2, ImmutableList.of(entity3));
		addReferences(entity3, ImmutableList.of(entity1, entity4));
		addReferences(entity4, ImmutableList.of());

		IndexDependencyModel indexDependencyModel = new IndexDependencyModel(entityTypes);
		Set<String> dependencies = indexDependencyModel.getEntityTypesDependentOn("0").collect(toSet());
		assertEquals(dependencies, ImmutableSet.of("1", "3", "4"));
	}

	@Test
	public void testGetEntityTypesDependentOnCircular() throws Exception
	{
		when(entity0.getIndexingDepth()).thenReturn(1);

		List<EntityType> entityTypes = ImmutableList.of(entity0);

		addReferences(entity0, ImmutableList.of(entity0));

		IndexDependencyModel indexDependencyModel = new IndexDependencyModel(entityTypes);
		Set<String> dependencies = indexDependencyModel.getEntityTypesDependentOn("0").collect(toSet());
		assertEquals(dependencies, ImmutableSet.of("0"));
	}

	@Test
	public void testGetEntityTypesDependentOnCircularZeroDepth() throws Exception
	{
		when(entity0.getIndexingDepth()).thenReturn(0);

		List<EntityType> entityTypes = ImmutableList.of(entity0);

		addReferences(entity0, ImmutableList.of(entity0));

		IndexDependencyModel indexDependencyModel = new IndexDependencyModel(entityTypes);
		Set<String> dependencies = indexDependencyModel.getEntityTypesDependentOn("0").collect(toSet());
		assertEquals(dependencies, ImmutableSet.of());
	}

	private void addReferences(EntityType referringEntity, List<EntityType> refEntities)
	{
		ImmutableList.Builder<Attribute> attributes = ImmutableList.builder();

		for (EntityType refEntity : refEntities)
		{
			Attribute attribute = Mockito.mock(Attribute.class);
			when(attribute.getRefEntity()).thenReturn(refEntity);
			attributes.add(attribute);
		}

		when(referringEntity.getOwnAtomicAttributes()).thenReturn(attributes.build());
	}

}