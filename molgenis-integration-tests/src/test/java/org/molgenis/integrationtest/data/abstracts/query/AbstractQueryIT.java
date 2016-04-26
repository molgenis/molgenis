package org.molgenis.integrationtest.data.abstracts.query;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.integrationtest.data.abstracts.AbstractDataIntegrationIT;

import com.google.common.collect.Lists;

import net.sf.samtools.util.RuntimeEOFException;

public abstract class AbstractQueryIT extends AbstractDataIntegrationIT
{
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	protected Entity person1;
	protected Entity person2;
	protected Entity person3;

	protected abstract void testInt();

	protected abstract void testDecimal();

	protected abstract void testLong();

	protected abstract void testString();

	protected abstract void testDate() throws ParseException;

	protected abstract void testDateTime() throws ParseException;

	protected abstract void testBool();

	protected abstract void testMref();

	protected abstract void testXref();

	protected Repository<Entity> getTestRepo()
	{
		// define model
		DefaultEntityMetaData countryEMD = new DefaultEntityMetaData("query_country");
		countryEMD.addAttribute("code", ROLE_ID).setNillable(false); // TODO: make this an enum!

		DefaultEntityMetaData bookEMD = new DefaultEntityMetaData("query_book");
		bookEMD.addAttribute("title", ROLE_ID).setNillable(false);

		DefaultEntityMetaData personEMD = new DefaultEntityMetaData("query_person");
		personEMD.addAttribute("id", ROLE_ID);
		personEMD.addAttribute("email").setNillable(false);
		personEMD.addAttribute("firstName");
		personEMD.addAttribute("lastName");
		personEMD.addAttribute("birthday").setDataType(DATE);
		personEMD.addAttribute("height").setDataType(INT);
		personEMD.addAttribute("active").setDataType(BOOL);
		personEMD.addAttribute("country").setDataType(XREF).setRefEntity(countryEMD);
		personEMD.addAttribute("authorOf").setDataType(MREF).setRefEntity(bookEMD);

		Repository<Entity> countries = dataService.getMeta().addEntityMeta(countryEMD);
		Repository<Entity> books = dataService.getMeta().addEntityMeta(bookEMD);
		Repository<Entity> persons = dataService.getMeta().addEntityMeta(personEMD);

		// add country entities to repo
		Entity c = new DefaultEntity(countryEMD, dataService);
		c.set("code", "US");
		countries.add(c);
		c.set("code", "NL");
		countries.add(c);

		// add book entities to repo
		Entity book1 = new DefaultEntity(bookEMD, dataService);
		book1.set("title", "MOLGENIS for dummies");
		Entity book2 = new DefaultEntity(bookEMD, dataService);
		book2.set("title", "Your database at the push of a button");

		books.add(book1);
		books.add(book2);

		// add person entities to repo
		try
		{
			person1 = new DefaultEntity(personEMD, dataService);
			person1.set("id", "person1");
			person1.set("email", "foo@localhost");
			person1.set("firstName", "john");
			person1.set("lastName", "doe");
			person1.set("birthday", (dateFormat.parse("1976-06-07")));
			person1.set("height", 180);
			person1.set("active", true);
			person1.set("country", "US");
			person1.set("authorOf", Lists.newArrayList(book1));
			persons.add(person1);

			person2 = new DefaultEntity(personEMD, dataService);
			person2.set("id", "person2");
			person2.set("email", "bar@localhost");
			person2.set("firstName", "jane");
			person2.set("lastName", "doe");
			person2.set("birthday", dateFormat.parse("1980-06-07"));
			person2.set("height", 165);
			person2.set("active", false);
			person2.set("country", "US");
			person2.set("authorOf", Lists.newArrayList(book1, book2));
			persons.add(person2);

			person3 = new DefaultEntity(personEMD, dataService);
			person3.set("id", "person3");
			person3.set("email", "donald@localhost");
			person3.set("firstName", "donald");
			person3.set("lastName", "duck");
			person3.set("birthday", dateFormat.parse("1950-01-31"));
			person3.set("height", 180);
			person3.set("active", true);
			person3.set("country", "NL");
			person3.set("authorOf", null);
			persons.add(person3);
		}
		catch (ParseException e)
		{
			throw new RuntimeEOFException(e);
		}

		return persons;
	}

	public void testIt() throws ParseException
	{
		testInt();
		testDecimal();
		testLong();
		testString();
		testDate();
		testDateTime();
		testXref();
		testMref();
	}
}
