package org.molgenis.data.meta;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.GenericDependencyResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class EntityTypeDependencyResolverTest
{
	private EntityTypeDependencyResolver entityTypeDependencyResolver;

	private EntityType entityType0;
	private EntityType entityType1;
	private EntityType entityType2;
	private EntityType entityType3;

	private Attribute attr0;
	private Attribute attr1;
	private Attribute attr2;
	private Attribute attr3;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		// do not mock generic dependency resolver to simplify test writing
		GenericDependencyResolver genericDependencyResolver = new GenericDependencyResolver();
		entityTypeDependencyResolver = new EntityTypeDependencyResolver(genericDependencyResolver);

		entityType0 = when(mock(EntityType.class).getId()).thenReturn("entity0").getMock();
		entityType1 = when(mock(EntityType.class).getId()).thenReturn("entity1").getMock();
		entityType2 = when(mock(EntityType.class).getId()).thenReturn("entity2").getMock();
		entityType3 = when(mock(EntityType.class).getId()).thenReturn("entity3").getMock();

		attr0 = when(mock(Attribute.class).getName()).thenReturn("attr0").getMock();
		attr1 = when(mock(Attribute.class).getName()).thenReturn("attr1").getMock();
		attr2 = when(mock(Attribute.class).getName()).thenReturn("attr2").getMock();
		attr3 = when(mock(Attribute.class).getName()).thenReturn("attr3").getMock();

		when(entityType0.getOwnAllAttributes()).thenReturn(singleton(attr0));
		when(entityType1.getOwnAllAttributes()).thenReturn(singleton(attr1));
		when(entityType2.getOwnAllAttributes()).thenReturn(singleton(attr2));
		when(entityType3.getOwnAllAttributes()).thenReturn(singleton(attr3));
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
		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(null);

		when(attr0.getRefEntity()).thenReturn(entityType0); // self-reference
		when(attr1.getRefEntity()).thenReturn(entityType2);
		when(attr1.isMappedBy()).thenReturn(true);
		when(attr1.getMappedBy()).thenReturn(attr2);
		when(attr2.getRefEntity()).thenReturn(entityType1);

		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entityType2, entityType1, entityType0)),
				newArrayList(entityType0, entityType1, entityType2));
	}

	@Test
	public void resolveSomeDependenciesInInput()
	{
		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(null);

		when(attr0.getRefEntity()).thenReturn(entityType0); // self-reference
		when(attr1.getRefEntity()).thenReturn(entityType2);
		when(attr1.isMappedBy()).thenReturn(true);
		when(attr1.getMappedBy()).thenReturn(attr2);
		when(attr2.getRefEntity()).thenReturn(entityType1);

		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entityType2, entityType0)),
				newArrayList(entityType0, entityType2));
	}

	@Test()
	public void resolveDependenciesExtends3Level()
	{
		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(entityType1);
		when(entityType3.getExtends()).thenReturn(entityType2);

		when(attr0.getRefEntity()).thenReturn(null);
		when(attr1.getRefEntity()).thenReturn(null);
		when(attr1.isMappedBy()).thenReturn(false);
		when(attr1.getMappedBy()).thenReturn(null);
		when(attr2.getRefEntity()).thenReturn(null);

		assertEquals(
				entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1, entityType2, entityType3)),
				newArrayList(entityType0, entityType1, entityType2, entityType3));

		assertEquals(
				entityTypeDependencyResolver.resolve(newArrayList(entityType1, entityType0, entityType2, entityType3)),
				newArrayList(entityType0, entityType1, entityType2, entityType3));

		assertEquals(
				entityTypeDependencyResolver.resolve(newArrayList(entityType1, entityType2, entityType0, entityType3)),
				newArrayList(entityType0, entityType1, entityType2, entityType3));

		assertEquals(
				entityTypeDependencyResolver.resolve(newArrayList(entityType1, entityType2, entityType3, entityType0)),
				newArrayList(entityType0, entityType1, entityType2, entityType3));
	}

	@Test()
	public void resolveDependenciesAddedNotDependedEntity()
	{
		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(entityType1);
		when(entityType3.getExtends()).thenReturn(entityType2);

		when(attr0.getRefEntity()).thenReturn(entityType0); // self-reference
		when(attr1.getRefEntity()).thenReturn(entityType2);
		when(attr1.isMappedBy()).thenReturn(true);
		when(attr1.getMappedBy()).thenReturn(attr2);
		when(attr2.getRefEntity()).thenReturn(entityType1);

		assertEquals(
				entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType2, entityType3, entityType1)),
				newArrayList(entityType0, entityType1, entityType2, entityType3));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Could not resolve dependencies of items \\[entity1, entity2, entity0, entity3\\]. Are there circular dependencies\\?")
	public void resolveDependenciesEntityCircularExtends()
	{
		when(entityType0.getExtends()).thenReturn(entityType3);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(entityType1);
		when(entityType3.getExtends()).thenReturn(entityType2);

		entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1));
	}

	@Test(expectedExceptions = MolgenisDataException.class, expectedExceptionsMessageRegExp = "Could not resolve dependencies of items \\[entity1, entity2, entity0\\]. Are there circular dependencies\\?")
	public void resolveDependenciesEntityCircularRefEntity()
	{
		when(attr0.getRefEntity()).thenReturn(entityType1);
		when(attr1.getRefEntity()).thenReturn(entityType2);
		when(attr2.getRefEntity()).thenReturn(entityType0);

		entityTypeDependencyResolver.resolve(newArrayList(entityType1, entityType2, entityType3, entityType0));
	}

	@Test()
	public void resolveDependenciesEntity()
	{
		EntityType entityType4 = when(mock(EntityType.class).getId()).thenReturn("entity4").getMock();
		Attribute attr4 = when(mock(Attribute.class).getName()).thenReturn("attr4").getMock();
		when(entityType4.getOwnAllAttributes()).thenReturn(singleton(attr4));

		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(entityType1);
		when(attr0.getRefEntity()).thenReturn(entityType3);
		when(attr3.getRefEntity()).thenReturn(entityType4);

		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1, entityType2)),
				newArrayList(entityType0, entityType1, entityType2));
	}

	@Test()
	public void resolveDependenciesEntityExtendedDependencies()
	{
		EntityType entityType4 = when(mock(EntityType.class).getId()).thenReturn("entity4").getMock();
		Attribute attr4 = when(mock(Attribute.class).getName()).thenReturn("attr4").getMock();
		when(entityType4.getOwnAllAttributes()).thenReturn(singleton(attr4));

		EntityType entityType5 = when(mock(EntityType.class).getId()).thenReturn("entity5").getMock();
		Attribute attr5 = when(mock(Attribute.class).getName()).thenReturn("attr5").getMock();
		when(entityType5.getOwnAllAttributes()).thenReturn(singleton(attr5));

		EntityType entityType6 = when(mock(EntityType.class).getId()).thenReturn("entity6").getMock();
		Attribute attr6 = when(mock(Attribute.class).getName()).thenReturn("attr6").getMock();
		when(entityType6.getOwnAllAttributes()).thenReturn(singleton(attr6));

		when(entityType0.getExtends()).thenReturn(null);
		when(entityType1.getExtends()).thenReturn(entityType0);
		when(entityType2.getExtends()).thenReturn(entityType1);
		when(attr0.getRefEntity()).thenReturn(entityType3);
		when(attr3.getRefEntity()).thenReturn(entityType4);
		when(entityType4.getExtends()).thenReturn(entityType5);
		when(entityType5.getExtends()).thenReturn(entityType6);

		assertEquals(entityTypeDependencyResolver.resolve(newArrayList(entityType0, entityType1, entityType2)),
				newArrayList(entityType0, entityType1, entityType2));
	}
}