package org.molgenis.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DependencyResolverTest
{

	@Test
	public void resolve()
	{
		DefaultEntityMetaData e1 = new DefaultEntityMetaData("e1");
		DefaultEntityMetaData e2 = new DefaultEntityMetaData("e2");
		DefaultEntityMetaData e3 = new DefaultEntityMetaData("e3");
		DefaultEntityMetaData e4 = new DefaultEntityMetaData("e4");
		DefaultEntityMetaData e5 = new DefaultEntityMetaData("e5");

		e1.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e5);
		e5.setExtends(e3);
		e3.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e4);
		e3.addAttribute("refSelf").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e3);
		e4.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e2);

		List<EntityMetaData> resolved = DependencyResolver
				.resolve(Sets.<EntityMetaData> newHashSet(e1, e2, e3, e4, e5));
		assertEquals(resolved, Arrays.asList(e2, e4, e3, e5, e1));
	}

	@Test
	public void resolveSelfReferences()
	{
		DefaultEntityMetaData emd = new DefaultEntityMetaData("Person");
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

		Repository repo = mock(Repository.class);
		when(repo.getName()).thenReturn("Person");
		when(repo.findOne("Piet")).thenReturn(piet);
		when(repo.findOne("Jan")).thenReturn(jan);
		when(repo.findOne("Marie")).thenReturn(marie);
		when(repo.findOne("Katrijn")).thenReturn(katrijn);

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
