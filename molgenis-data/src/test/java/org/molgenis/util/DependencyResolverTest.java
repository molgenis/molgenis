package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DependencyResolverTest
{

	@Test
	public void resolve()
	{
		EntityMetaData e1 = new EntityMetaDataImpl("e1");
		EntityMetaData e2 = new EntityMetaDataImpl("e2");
		EntityMetaData e3 = new EntityMetaDataImpl("e3");
		EntityMetaData e4 = new EntityMetaDataImpl("e4");
		EntityMetaData e5 = new EntityMetaDataImpl("e5");

		e1.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e5);
		e5.setExtends(e3);
		e3.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e4);
		e3.addAttribute("refSelf").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e3);
		e4.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e2);

		List<EntityMetaData> resolved = DependencyResolver
				.resolve(Sets.newHashSet(e1, e2, e3, e4, e5));
		assertEquals(resolved, Arrays.asList(e2, e4, e3, e5, e1));
	}

	@Test
	public void resolveSelfReferences()
	{
		EntityMetaData emd = new EntityMetaDataImpl("Person");
		emd.addAttribute("name", ROLE_ID);
		emd.addAttribute("father").setDataType(MolgenisFieldTypes.XREF).setNillable(true).setRefEntity(emd);
		emd.addAttribute("mother").setDataType(MolgenisFieldTypes.XREF).setNillable(true).setRefEntity(emd);

		Entity piet = new MapEntity("name");
		piet.set("name", "Piet");

		Entity klaas = new MapEntity("name");
		klaas.set("name", "Klaas");
		klaas.set("father", "Piet");
		klaas.set("mother", "Katrijn");

		Entity jan = new MapEntity("name");
		jan.set("name", "Jan");
		jan.set("father", "Piet");
		jan.set("mother", "Marie");

		Entity katrijn = new MapEntity("name");
		katrijn.set("name", "Katrijn");
		katrijn.set("father", "Jan");
		katrijn.set("mother", "Marie");

		Entity marie = new MapEntity("name");
		marie.set("name", "Marie");

		Repository<Entity> repo = mock(Repository.class);
		when(repo.getName()).thenReturn("Person");
		when(repo.findOneById("Piet")).thenReturn(piet);
		when(repo.findOneById("Jan")).thenReturn(jan);
		when(repo.findOneById("Marie")).thenReturn(marie);
		when(repo.findOneById("Katrijn")).thenReturn(katrijn);

		DataServiceImpl ds = new DataServiceImpl();
		ds.addRepository(repo);

		Iterable<Entity> entities = Arrays.<Entity> asList(new DefaultEntity(emd, ds, piet),
				new DefaultEntity(emd, ds, klaas), new DefaultEntity(emd, ds, jan), new DefaultEntity(emd, ds, katrijn),
				new DefaultEntity(emd, ds, marie));

		Iterable<Entity> sorted = new DependencyResolver().resolveSelfReferences(entities, emd);
		List<Entity> sortedList = Lists.newArrayList(sorted);
		assertEquals(sortedList.size(), 5);
		assertEquals(sortedList.get(0).getIdValue(), "Marie");
		assertEquals(sortedList.get(1).getIdValue(), "Piet");
		assertEquals(sortedList.get(2).getIdValue(), "Jan");
		assertEquals(sortedList.get(3).getIdValue(), "Katrijn");
		assertEquals(sortedList.get(4).getIdValue(), "Klaas");
	}
}
