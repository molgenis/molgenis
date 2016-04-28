package org.molgenis.integrationtest.data.abstracts.query;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.LONG;
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
	static final String FIRST_NAME = "firstName";
	static final String LAST_NAME = "lastName";
	static final String BIRTHDAY = "birthday";
	static final String HEIGHT = "height";
	static final String ACTIVE = "active";
	static final String COUNTRY = "country";
	static final String AUTHOR_OF = "authorOf";
	static final String ID = "id";
	static final String EMAIL = "email";
	static final String ACCOUNT_BALANCE = "accountBalance";
	static final String SERIAL_NUMBER = "serialNumber";
	static final String BIRTH_TIME = "birthTime";

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Entity person1;
	Entity person2;
	Entity person3;
	Repository<Entity> personsRepository;

	abstract void testInt();

	abstract void testDecimal();

	abstract void testLong();

	abstract void testString();

	abstract void testDate() throws ParseException;

	abstract void testDateTime() throws ParseException;

	abstract void testBool();

	abstract void testMref();

	abstract void testXref();

	public void testIt() throws ParseException
	{
		personsRepository = createTestRepo();

		testInt();
		testDecimal();
		testLong();
		testString();
		testDate();
		testDateTime();
		testBool();
		testXref();
		testMref();
	}

	private Repository<Entity> createTestRepo()
	{
		DefaultEntityMetaData countryEMD = new DefaultEntityMetaData("query_country");
		countryEMD.addAttribute("code", ROLE_ID).setNillable(false);

		DefaultEntityMetaData bookEMD = new DefaultEntityMetaData("query_book");
		bookEMD.addAttribute("title", ROLE_ID).setNillable(false);

		DefaultEntityMetaData personEMD = new DefaultEntityMetaData("query_person");
		personEMD.addAttribute(ID, ROLE_ID);
		personEMD.addAttribute(EMAIL).setNillable(false);
		personEMD.addAttribute(FIRST_NAME);
		personEMD.addAttribute(LAST_NAME);
		personEMD.addAttribute(BIRTHDAY).setDataType(DATE);
		personEMD.addAttribute(BIRTH_TIME).setDataType(DATETIME);
		personEMD.addAttribute(HEIGHT).setDataType(INT);
		personEMD.addAttribute(ACTIVE).setDataType(BOOL);
		personEMD.addAttribute(COUNTRY).setDataType(XREF).setRefEntity(countryEMD);
		personEMD.addAttribute(AUTHOR_OF).setDataType(MREF).setRefEntity(bookEMD);
		personEMD.addAttribute(ACCOUNT_BALANCE).setDataType(DECIMAL);
		personEMD.addAttribute(SERIAL_NUMBER).setDataType(LONG);

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
			person1.set(ID, "person1");
			person1.set(EMAIL, "foo@localhost");
			person1.set(FIRST_NAME, "john");
			person1.set(LAST_NAME, "doe");
			person1.set(BIRTHDAY, dateFormat.parse("1976-06-07"));
			person1.set(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"));
			person1.set(HEIGHT, 180);
			person1.set(ACTIVE, true);
			person1.set(COUNTRY, "US");
			person1.set(AUTHOR_OF, newArrayList(book1));
			person1.set(ACCOUNT_BALANCE, 299.99);
			person1.set(SERIAL_NUMBER, 374278348334L);
			persons.add(person1);

			person2 = new DefaultEntity(personEMD, dataService);
			person2.set(ID, "person2");
			person2.set(EMAIL, "bar@localhost");
			person2.set(FIRST_NAME, "jane");
			person2.set(LAST_NAME, "doe");
			person2.set(BIRTHDAY, dateFormat.parse("1980-06-07"));
			person2.set(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07"));
			person2.set(HEIGHT, 165);
			person2.set(ACTIVE, false);
			person2.set(COUNTRY, "US");
			person2.set(AUTHOR_OF, newArrayList(book1, book2));
			person2.set(ACCOUNT_BALANCE, -0.70);
			person2.set(SERIAL_NUMBER, 67986789879L);
			persons.add(person2);

			person3 = new DefaultEntity(personEMD, dataService);
			person3.set(ID, "person3");
			person3.set(EMAIL, "donald@localhost");
			person3.set(FIRST_NAME, "donald");
			person3.set(LAST_NAME, "duck");
			person3.set(BIRTHDAY, dateFormat.parse("1950-01-31"));
			person3.set(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:08"));
			person3.set(HEIGHT, 180);
			person3.set(ACTIVE, true);
			person3.set(COUNTRY, "NL");
			person3.set(AUTHOR_OF, null);
			person3.set(ACCOUNT_BALANCE, 1000.00);
			person3.set(SERIAL_NUMBER, 23471900909L);
			persons.add(person3);
		}
		catch (ParseException e)
		{
			throw new RuntimeEOFException(e);
		}

		return persons;
	}
}