package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.util.GenericDependencyResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.testng.Assert.assertEquals;

public class EntityMetaDataDependencyResolverTest
{
	private EntityMetaDataDependencyResolver entityMetaDependencyResolver;

	private EntityMetaData entityMeta0;
	private EntityMetaData entityMeta1;
	private EntityMetaData entityMeta2;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		// do not mock generic dependency resolver to simplify test writing
		GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();
		entityMetaDependencyResolver = new EntityMetaDataDependencyResolver(genericDependencyResolver);

		entityMeta0 = when(mock(EntityMetaData.class).getName()).thenReturn("entity0").getMock();
		entityMeta1 = when(mock(EntityMetaData.class).getName()).thenReturn("entity1").getMock();
		entityMeta2 = when(mock(EntityMetaData.class).getName()).thenReturn("entity2").getMock();

		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		Attribute attr2 = when(mock(Attribute.class).getName()).thenReturn("attr2").getMock();

		when(entityMeta0.getOwnAllAttributes()).thenReturn(singleton(attr0));
		when(entityMeta1.getOwnAllAttributes()).thenReturn(singleton(attr1));
		when(entityMeta2.getOwnAllAttributes()).thenReturn(singleton(attr2));

		when(entityMeta0.getExtends()).thenReturn(null);
		when(entityMeta1.getExtends()).thenReturn(entityMeta0);
		when(entityMeta2.getExtends()).thenReturn(null);

		when(attr0.getRefEntity()).thenReturn(entityMeta0); // self-reference
		when(attr1.getRefEntity()).thenReturn(entityMeta2);
		when(attr1.isMappedBy()).thenReturn(true);
		when(attr1.getMappedBy()).thenReturn(attr2);
		when(attr2.getRefEntity()).thenReturn(entityMeta1);
	}

	@Test
	public void resolveDependenciesEmptyInput()
	{
		List<EntityMetaData> entityMetas = emptyList();
		List<EntityMetaData> resolvedEntityMetas = entityMetaDependencyResolver.resolve(entityMetas);
		assertEquals(resolvedEntityMetas, emptyList());
	}

	@Test
	public void resolveDependenciesOneItemInput()
	{
		List<EntityMetaData> entityMetas = singletonList(entityMeta0);
		List<EntityMetaData> resolvedEntityMetas = entityMetaDependencyResolver.resolve(entityMetas);
		assertEquals(resolvedEntityMetas, entityMetas);
	}

	@Test
	public void resolveDependenciesInInput()
	{
		assertEquals(entityMetaDependencyResolver.resolve(newArrayList(entityMeta2, entityMeta1, entityMeta0)),
				newArrayList(entityMeta0, entityMeta1, entityMeta2));
	}

	@Test
	public void resolveSomeDependenciesInInput()
	{
		assertEquals(entityMetaDependencyResolver.resolve(newArrayList(entityMeta2, entityMeta0)),
				newArrayList(entityMeta0, entityMeta2));
	}

	@Test
	public void resolveDependenciesEntityAttributeWorkaround()
	{
		EntityMetaData entitiesMeta = when(mock(EntityMetaData.class).getName()).thenReturn(ENTITY_META_DATA).getMock();
		EntityMetaData attrsMeta = when(mock(EntityMetaData.class).getName()).thenReturn(ATTRIBUTE_META_DATA).getMock();

		Attribute entitiesAttr = when(mock(Attribute.class).getName()).thenReturn("attrs").getMock();
		when(entitiesAttr.getRefEntity()).thenReturn(attrsMeta);
		Attribute attrAttr = when(mock(Attribute.class).getName()).thenReturn("entity").getMock();
		when(attrAttr.getRefEntity()).thenReturn(entitiesMeta);

		when(entitiesMeta.getOwnAllAttributes()).thenReturn(singleton(entitiesAttr));
		when(attrsMeta.getOwnAllAttributes()).thenReturn(singleton(attrAttr));

		assertEquals(entityMetaDependencyResolver.resolve(newArrayList(entitiesMeta, attrsMeta)),
				newArrayList(attrsMeta, entitiesMeta));
	}
}