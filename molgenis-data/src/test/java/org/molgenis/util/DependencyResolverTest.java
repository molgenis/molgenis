package org.molgenis.util;

import com.google.common.collect.Lists;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.Test;

import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

public class DependencyResolverTest
{
	@Test
	public void resolve()
	{
		EntityMetaData e1 = when(mock(EntityMetaData.class).getName()).thenReturn("e1").getMock();
		EntityMetaData e2 = when(mock(EntityMetaData.class).getName()).thenReturn("e2").getMock();
		EntityMetaData e3 = when(mock(EntityMetaData.class).getName()).thenReturn("e3").getMock();
		EntityMetaData e4 = when(mock(EntityMetaData.class).getName()).thenReturn("e4").getMock();
		EntityMetaData e5 = when(mock(EntityMetaData.class).getName()).thenReturn("e5").getMock();

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

		List<EntityMetaData> resolved = DependencyResolver.resolve(newHashSet(e1, e2, e3, e4, e5));
		assertEquals(resolved, asList(e2, e4, e3, e5, e1));
	}

	@Test
	public void resolveSelfReferences()
	{
		EntityMetaData emd = when(mock(EntityMetaData.class).getName()).thenReturn("Person").getMock();
		Attribute nameAttr = when(mock(Attribute.class).getName()).thenReturn("name").getMock();
		when(nameAttr.getDataType()).thenReturn(STRING);
		Attribute fatherAttr = when(mock(Attribute.class).getName()).thenReturn("father").getMock();
		when(fatherAttr.getDataType()).thenReturn(XREF);
		when(fatherAttr.getRefEntity()).thenReturn(emd);
		when(fatherAttr.isNillable()).thenReturn(true);
		Attribute motherAttr = when(mock(Attribute.class).getName()).thenReturn("mother").getMock();
		when(emd.getIdAttribute()).thenReturn(nameAttr);
		when(motherAttr.getDataType()).thenReturn(XREF);
		when(motherAttr.isNillable()).thenReturn(true);
		when(motherAttr.getRefEntity()).thenReturn(emd);
		when(emd.getAtomicAttributes()).thenReturn(asList(nameAttr, fatherAttr, motherAttr));
		when(emd.getAttribute("name")).thenReturn(nameAttr);
		when(emd.getAttribute("father")).thenReturn(fatherAttr);
		when(emd.getAttribute("mother")).thenReturn(motherAttr);

		Entity piet = new DynamicEntity(emd);
		piet.set("name", "Piet");
		Entity klaas = new DynamicEntity(emd);
		klaas.set("name", "Klaas");
		Entity jan = new DynamicEntity(emd);
		jan.set("name", "Jan");
		Entity katrijn = new DynamicEntity(emd);
		katrijn.set("name", "Katrijn");
		Entity marie = new DynamicEntity(emd);
		marie.set("name", "Marie");

		klaas.set("father", piet);
		klaas.set("mother", katrijn);

		jan.set("father", piet);
		jan.set("mother", marie);

		katrijn.set("father", jan);
		katrijn.set("mother", marie);

		Iterable<Entity> entities = asList(piet, klaas, jan, katrijn, marie);

		Iterable<Entity> sorted = new DependencyResolver().resolveSelfReferences(entities, emd);
		assertEquals(Lists.newArrayList(sorted), asList(marie, piet, jan, katrijn, klaas));
	}
}
