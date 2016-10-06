package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.GenericDependencyResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_META_DATA;
import static org.testng.Assert.assertEquals;

public class EntityTypeDependencyResolverTest
{
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	private EntityType entityType0;
	private EntityType entityType1;
	private EntityType entityType2;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		// do not mock generic dependency resolver to simplify test writing
		GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();
		entityTypeDependencyResolver = new EntityTypeDependencyResolver(genericDependencyResolver);

		entityType0 = when(mock(EntityType.class).getName()).thenReturn("entity0").getMock();
		entityType1 = when(mock(EntityType.class).getName()).thenReturn("entity1").getMock();
		entityType2 = when(mock(EntityType.class).getName()).thenReturn("entity2").getMock();

		Attribute attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		Attribute attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		Attribute attr2 = when(mock(Attribute.class).getName()).thenReturn("attr2").getMock();

		when(entityType0.getOwnAllAttributes()).thenReturn(singleton(attr0));
		when(entityType1.getOwnAllAttributes()).thenReturn(singleton(attr1));
		when(entityType2.getOwnAllAttributes()).thenReturn(singleton(attr2));

		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(null);

		when(attr0.getRefEntity()).thenReturn(entityType0); // self-reference
		when(attr1.getRefEntity()).thenReturn(entityType2);
		when(attr1.isMappedBy()).thenReturn(true);
		when(attr1.getMappedBy()).thenReturn(attr2);
		when(attr2.getRefEntity()).thenReturn(entityType1);
	}

	@Test
	public void resolveDependenciesEmptyInput()
	{
		List<EntityType> entityTypes = emptyList();
		List<EntityType> resolvedEntityMetas = entityTypeDependencyResolver.resolve(entityTypes);
		assertEquals(resolvedEntityMetas, emptyList());
	}

	@Test
	public void resolveDependenciesOneItemInput()
	{
		List<EntityType> entityTypes = singletonList(entityType0);
		List<EntityType> resolvedEntityMetas = entityTypeDependencyResolver.resolve(entityTypes);
		assertEquals(resolvedEntityMetas, entityTypes);
	}

	@Test
	public void resolveDependenciesInInput()
	{
		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entityType2, entityType1, entityType0)),
				newArrayList(entityType0, entityType1, entityType2));
	}

	@Test
	public void resolveSomeDependenciesInInput()
	{
		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entityType2, entityType0)),
				newArrayList(entityType0, entityType2));
	}

	@Test
	public void resolveDependenciesEntityAttributeWorkaround()
	{
		EntityType entitiesMeta = when(mock(EntityType.class).getName()).thenReturn(ENTITY_META_DATA).getMock();
		EntityType attrsMeta = when(mock(EntityType.class).getName()).thenReturn(ATTRIBUTE_META_DATA).getMock();

		Attribute entitiesAttr = when(mock(Attribute.class).getName()).thenReturn("attrs").getMock();
		when(entitiesAttr.getRefEntity()).thenReturn(attrsMeta);
		Attribute attrAttr = when(mock(Attribute.class).getName()).thenReturn("entity").getMock();
		when(attrAttr.getRefEntity()).thenReturn(entitiesMeta);

		when(entitiesMeta.getOwnAllAttributes()).thenReturn(singleton(entitiesAttr));
		when(attrsMeta.getOwnAllAttributes()).thenReturn(singleton(attrAttr));

		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entitiesMeta, attrsMeta)),
				newArrayList(attrsMeta, entitiesMeta));
	}
}