package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.QueryImpl;

public abstract class AbstractCountIT extends AbstractDataIntegrationIT
{
	public void testIt() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		// define model
		EntityMetaData countryMD = new EntityMetaDataImpl("query_country");
		countryMD.addAttribute("code", ROLE_ID).setNillable(false); // TODO: make this an enum!

		EntityMetaData personMD = new EntityMetaDataImpl("query_person");
		personMD.addAttribute("email", ROLE_ID).setNillable(false);
		personMD.addAttribute("firstName");
		personMD.addAttribute("lastName");
		personMD.addAttribute("birthday").setDataType(DATE);
		personMD.addAttribute("height").setDataType(INT);
		personMD.addAttribute("active").setDataType(BOOL);
		personMD.addAttribute("country").setDataType(XREF).setRefEntity(countryMD);

		Repository<Entity> countries = dataService.getMeta().addEntityMeta(countryMD);
		Repository<Entity> persons = dataService.getMeta().addEntityMeta(personMD);

		// add country entities to repo
		Entity c = new DefaultEntity(countryMD, dataService);
		c.set("code", "US");
		countries.add(c);
		c.set("code", "NL");
		countries.add(c);

		Entity e = new DefaultEntity(personMD, dataService);
		e.set("email", "foo@localhost");
		e.set("firstName", "john");
		e.set("lastName", "doe");
		e.set("birthday", (sdf.parse("1976-06-07")));
		e.set("height", 180);
		e.set("active", true);
		e.set("country", "US");
		persons.add(e);

		// add person entities to repo
		e.set("email", "bar@localhost");
		e.set("firstName", "jane");
		e.set("lastName", "doe");
		e.set("birthday", sdf.parse("1980-06-07"));
		e.set("height", 165);
		e.set("active", false);
		e.set("country", "US");
		persons.add(e);

		e.set("email", "donald@localhost");
		e.set("firstName", "donald");
		e.set("lastName", "duck");
		e.set("birthday", sdf.parse("1950-01-31"));
		e.set("height", 55);
		e.set("active", true);
		e.set("country", "NL");
		persons.add(e);

		// query test
		assertEquals(persons.count(), 3);

		// search all text/string fields
		assertEquals(persons.count(new QueryImpl<Entity>().search("doe")), 2);

		// string
		assertEquals(persons.count(new QueryImpl<Entity>().eq("lastName", "doe")), 2);
		assertEquals(persons.count(new QueryImpl<Entity>().eq("lastName", "duck")), 1);
		assertEquals(persons.count(new QueryImpl<Entity>().eq("lastName", "duck").or().eq("lastName", "doe")), 3);

		// int
		assertEquals(persons.count(new QueryImpl<Entity>().eq("height", 180)), 1);
		assertEquals(persons.count(new QueryImpl<Entity>().lt("height", 180)), 2);
		assertEquals(persons.count(new QueryImpl<Entity>().le("height", 180)), 3);
		assertEquals(persons.count(new QueryImpl<Entity>().lt("height", 180).and().gt("height", 55)), 1);
		assertEquals(persons.count(new QueryImpl<Entity>().gt("height", 165).or().lt("height", 165)), 2);

		// bool
		assertEquals(persons.count(new QueryImpl<Entity>().eq("active", true)), 2);
		assertEquals(persons.count(new QueryImpl<Entity>().eq("active", false)), 1);
		assertEquals(persons.count(new QueryImpl<Entity>().eq("active", true).or().eq("height", 165)), 3);

		// date
		assertEquals(persons.count(new QueryImpl<Entity>().eq("birthday", sdf.parse("1950-01-31"))), 1);
		assertEquals(persons.count(new QueryImpl<Entity>().gt("birthday", sdf.parse("1950-01-31"))), 2);
		assertEquals(
				persons.count(new QueryImpl<Entity>().gt("birthday", sdf.parse("1976-06-07")).or()
						.lt("birthday", sdf.parse("1976-06-07"))), 2);

		// xref
		assertEquals(persons.count(new QueryImpl<Entity>().eq("country", "US")), 2);
		assertEquals(persons.count(new QueryImpl<Entity>().eq("country", "NL")), 1);
		assertEquals(persons.count(new QueryImpl<Entity>().eq("country", "US").and().gt("height", 165)), 1);
	}
}
