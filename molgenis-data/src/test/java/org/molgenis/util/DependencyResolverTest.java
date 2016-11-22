package org.molgenis.util;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;

public class DependencyResolverTest
{
	@Test
	public void resolveOneToMany()
	{
		EntityType oneToManyEntityType = when(mock(EntityType.class).getName()).thenReturn("entity").getMock();
		EntityType xrefEntityType = when(mock(EntityType.class).getName()).thenReturn("refEntity").getMock();

		Attribute oneToManyAttr = mock(Attribute.class);
		when(oneToManyAttr.getName()).thenReturn("oneToManyAttr");
		when(oneToManyAttr.getDataType()).thenReturn(ONE_TO_MANY);
		when(oneToManyAttr.getRefEntity()).thenReturn(xrefEntityType);

		Attribute xrefAttr = mock(Attribute.class);
		when(xrefAttr.getName()).thenReturn("xrefAttr");
		when(xrefAttr.getDataType()).thenReturn(XREF);
		when(xrefAttr.getRefEntity()).thenReturn(oneToManyEntityType);
		when(xrefAttr.getInversedBy()).thenReturn(oneToManyAttr);

		when(oneToManyAttr.getMappedBy()).thenReturn(xrefAttr);

		when(oneToManyEntityType.getAtomicAttributes()).thenReturn(singleton(oneToManyAttr));
		when(xrefEntityType.getAtomicAttributes()).thenReturn(singleton(xrefAttr));
		assertEquals(DependencyResolver.resolve(newLinkedHashSet(newArrayList(xrefEntityType, oneToManyEntityType))),
				newArrayList(oneToManyEntityType, xrefEntityType));
	}

	@Test
	public void resolve()
	{
		EntityType e1 = when(mock(EntityType.class).getName()).thenReturn("e1").getMock();
		EntityType e2 = when(mock(EntityType.class).getName()).thenReturn("e2").getMock();
		EntityType e3 = when(mock(EntityType.class).getName()).thenReturn("e3").getMock();
		EntityType e4 = when(mock(EntityType.class).getName()).thenReturn("e4").getMock();
		EntityType e5 = when(mock(EntityType.class).getName()).thenReturn("e5").getMock();

		when(e1.toString()).thenReturn("e1");
		when(e2.toString()).thenReturn("e2");
		when(e3.toString()).thenReturn("e3");
		when(e4.toString()).thenReturn("e4");
		when(e5.toString()).thenReturn("e5");

		Attribute e1RefAttr = when(mock(Attribute.class).getName()).thenReturn("ref").getMock();
		when(e1RefAttr.getDataType()).thenReturn(XREF);
		when(e1RefAttr.getRefEntity()).thenReturn(e5);

		Attribute e3RefAttr = when(mock(Attribute.class).getName()).thenReturn("ref").getMock();
		when(e3RefAttr.getDataType()).thenReturn(XREF);
		when(e3RefAttr.getRefEntity()).thenReturn(e4);

		Attribute e3SelfRefAttr = when(mock(Attribute.class).getName()).thenReturn("refSelf").getMock();
		when(e3SelfRefAttr.getDataType()).thenReturn(XREF);
		when(e3SelfRefAttr.getRefEntity()).thenReturn(e3);

		Attribute e4RefAttr = when(mock(Attribute.class).getName()).thenReturn("ref").getMock();
		when(e4RefAttr.getDataType()).thenReturn(XREF);
		when(e4RefAttr.getRefEntity()).thenReturn(e2);

		when(e5.getExtends()).thenReturn(e3);

		when(e1.getAtomicAttributes()).thenReturn(asList(e1RefAttr));
		when(e2.getAtomicAttributes()).thenReturn(emptyList());
		when(e3.getAtomicAttributes()).thenReturn(asList(e3RefAttr, e3SelfRefAttr));
		when(e4.getAtomicAttributes()).thenReturn(asList(e4RefAttr));
		when(e5.getAtomicAttributes()).thenReturn(asList(e3RefAttr, e3SelfRefAttr));

		List<EntityType> resolved = DependencyResolver.resolve(newHashSet(e1, e2, e3, e4, e5));
		assertEquals(resolved, asList(e2, e4, e3, e5, e1));
	}

	@Test
	public void resolveSelfReferences()
	{
		EntityType entityType = when(mock(EntityType.class).getName()).thenReturn("Person").getMock();
		Attribute nameAttr = when(mock(Attribute.class).getName()).thenReturn("name").getMock();

		when(nameAttr.getDataType()).thenReturn(STRING);
		Attribute fatherAttr = when(mock(Attribute.class).getName()).thenReturn("father").getMock();
		when(fatherAttr.getDataType()).thenReturn(XREF);
		when(fatherAttr.getRefEntity()).thenReturn(entityType);
		when(fatherAttr.isNillable()).thenReturn(true);
		Attribute motherAttr = when(mock(Attribute.class).getName()).thenReturn("mother").getMock();
		when(entityType.getIdAttribute()).thenReturn(nameAttr);
		when(motherAttr.getDataType()).thenReturn(XREF);
		when(motherAttr.isNillable()).thenReturn(true);
		when(motherAttr.getRefEntity()).thenReturn(entityType);
		when(entityType.getAtomicAttributes()).thenReturn(asList(nameAttr, fatherAttr, motherAttr));
		when(entityType.getAttribute("name")).thenReturn(nameAttr);
		when(entityType.getAttribute("father")).thenReturn(fatherAttr);
		when(entityType.getAttribute("mother")).thenReturn(motherAttr);

		Entity piet = new DynamicEntity(entityType);
		piet.set("name", "Piet");
		Entity klaas = new DynamicEntity(entityType);
		klaas.set("name", "Klaas");
		Entity jan = new DynamicEntity(entityType);
		jan.set("name", "Jan");
		Entity katrijn = new DynamicEntity(entityType);
		katrijn.set("name", "Katrijn");
		Entity marie = new DynamicEntity(entityType);
		marie.set("name", "Marie");

		klaas.set("father", piet);
		klaas.set("mother", katrijn);

		jan.set("father", piet);
		jan.set("mother", marie);

		katrijn.set("father", jan);
		katrijn.set("mother", marie);

		Iterable<Entity> entities = asList(piet, klaas, jan, katrijn, marie);

		Iterable<Entity> sorted = new DependencyResolver().resolveSelfReferences(entities, entityType);
		assertEquals(newArrayList(sorted), asList(marie, piet, jan, katrijn, klaas));
	}
}
