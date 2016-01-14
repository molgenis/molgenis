package org.molgenis.integrationtest.data;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.testng.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;

public abstract class AbstractCountTest extends AbstractDataIntegrationTest
{
	public void testIt() throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		// define model
		DefaultEntityMetaData countryMD = new DefaultEntityMetaData("query_country");
		countryMD.addAttribute("code").setNillable(false).setIdAttribute(true); // TODO: make this an enum!

		DefaultEntityMetaData personMD = new DefaultEntityMetaData("query_person");
		personMD.addAttribute("email").setNillable(false).setIdAttribute(true);
		personMD.addAttribute("firstName");
		personMD.addAttribute("lastName");
		personMD.addAttribute("birthday").setDataType(DATE);
		personMD.addAttribute("height").setDataType(INT);
		personMD.addAttribute("active").setDataType(BOOL);
		personMD.addAttribute("country").setDataType(XREF).setRefEntity(countryMD);

		Repository countries = dataService.getMeta().addEntityMeta(countryMD);
		Repository persons = dataService.getMeta().addEntityMeta(personMD);

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
		assertEquals(persons.count(new QueryImpl().search("doe")), 2);

		// string
		assertEquals(persons.count(new QueryImpl().eq("lastName", "doe")), 2);
		assertEquals(persons.count(new QueryImpl().eq("lastName", "duck")), 1);
		assertEquals(persons.count(new QueryImpl().eq("lastName", "duck").or().eq("lastName", "doe")), 3);

		// int
		assertEquals(persons.count(new QueryImpl().eq("height", 180)), 1);
		assertEquals(persons.count(new QueryImpl().lt("height", 180)), 2);
		assertEquals(persons.count(new QueryImpl().le("height", 180)), 3);
		assertEquals(persons.count(new QueryImpl().lt("height", 180).and().gt("height", 55)), 1);
		assertEquals(persons.count(new QueryImpl().gt("height", 165).or().lt("height", 165)), 2);

		// bool
		assertEquals(persons.count(new QueryImpl().eq("active", true)), 2);
		assertEquals(persons.count(new QueryImpl().eq("active", false)), 1);
		assertEquals(persons.count(new QueryImpl().eq("active", true).or().eq("height", 165)), 3);

		// date
		assertEquals(persons.count(new QueryImpl().eq("birthday", sdf.parse("1950-01-31"))), 1);
		assertEquals(persons.count(new QueryImpl().gt("birthday", sdf.parse("1950-01-31"))), 2);
		assertEquals(
				persons.count(new QueryImpl().gt("birthday", sdf.parse("1976-06-07")).or()
						.lt("birthday", sdf.parse("1976-06-07"))), 2);

		// xref
		assertEquals(persons.count(new QueryImpl().eq("country", "US")), 2);
		assertEquals(persons.count(new QueryImpl().eq("country", "NL")), 1);
		assertEquals(persons.count(new QueryImpl().eq("country", "US").and().gt("height", 165)), 1);
	}
}
