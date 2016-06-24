package org.molgenis.integrationtest.data.abstracts.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.integrationtest.data.abstracts.AbstractDataIntegrationIT;

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
	static final String QUOTE = "catchPhrase";
	static final String NUMBER = "number";

	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Entity person1;
	Entity person2;
	Entity person3;
	Repository<Entity> personsRepository;

	public void testIt() throws ParseException
	{
		createTestRepo();

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

	protected void createTestRepo()
	{
		//		DefaultEntityMetaData countryEMD = new DefaultEntityMetaData("query_country");
		//		countryEMD.addAttribute("code", ROLE_ID).setNillable(false);
		//
		//		DefaultEntityMetaData bookEMD = new DefaultEntityMetaData("query_book");
		//		bookEMD.addAttribute("title", ROLE_ID).setDataType(STRING).setNillable(false);
		//
		//		DefaultEntityMetaData numberEMD = new DefaultEntityMetaData("query_number");
		//		numberEMD.addAttribute("id", ROLE_ID).setDataType(INT).setNillable(false);
		//
		//		DefaultEntityMetaData personEMD = new DefaultEntityMetaData("query_person");
		//		personEMD.addAttribute(ID, ROLE_ID);
		//		personEMD.addAttribute(EMAIL).setNillable(false);
		//		personEMD.addAttribute(FIRST_NAME);
		//		personEMD.addAttribute(LAST_NAME);
		//		personEMD.addAttribute(BIRTHDAY).setDataType(DATE);
		//		personEMD.addAttribute(BIRTH_TIME).setDataType(DATETIME);
		//		personEMD.addAttribute(HEIGHT).setDataType(INT);
		//		personEMD.addAttribute(ACTIVE).setDataType(BOOL);
		//		personEMD.addAttribute(COUNTRY).setDataType(XREF).setRefEntity(countryEMD);
		//		personEMD.addAttribute(AUTHOR_OF).setDataType(MREF).setRefEntity(bookEMD);
		//		personEMD.addAttribute(ACCOUNT_BALANCE).setDataType(DECIMAL);
		//		personEMD.addAttribute(SERIAL_NUMBER).setDataType(LONG);
		//		personEMD.addAttribute(QUOTE).setDataType(STRING);
		//		personEMD.addAttribute(NUMBER).setDataType(MREF).setRefEntity(numberEMD);
		//
		//		Repository<Entity> countries = dataService.getMeta().addEntityMeta(countryEMD);
		//		Repository<Entity> books = dataService.getMeta().addEntityMeta(bookEMD);
		//		Repository<Entity> numbers = dataService.getMeta().addEntityMeta(numberEMD);
		//		personsRepository = dataService.getMeta().addEntityMeta(personEMD);
		//
		//		// add country entities to repo
		//		Entity c = new DefaultEntity(countryEMD, dataService);
		//		c.set("code", "US");
		//		countries.add(c);
		//		c.set("code", "NL");
		//		countries.add(c);
		//
		//		// add book entities to repo
		//		Entity book1 = new DefaultEntity(bookEMD, dataService);
		//		book1.set("title", "MOLGENIS for dummies");
		//		Entity book2 = new DefaultEntity(bookEMD, dataService);
		//		book2.set("title", "Your database at the push of a button");
		//		books.add(book1);
		//		books.add(book2);
		//
		//		// add number entities to repo
		//		Entity number1 = new DefaultEntity(numberEMD, dataService);
		//		number1.set("id", 11);
		//		Entity number2 = new DefaultEntity(numberEMD, dataService);
		//		number2.set("id", 22);
		//		Entity number3 = new DefaultEntity(numberEMD, dataService);
		//		number3.set("id", 33);
		//		Entity number4 = new DefaultEntity(numberEMD, dataService);
		//		number4.set("id", 44);
		//		numbers.add(number1);
		//		numbers.add(number2);
		//		numbers.add(number3);
		//		numbers.add(number4);
		//
		//		// add person entities to repo
		//		try
		//		{
		//			person1 = new DefaultEntity(personEMD, dataService);
		//			person1.set(ID, "person1");
		//			person1.set(EMAIL, "foo@localhost");
		//			person1.set(FIRST_NAME, "john");
		//			person1.set(LAST_NAME, "doe");
		//			person1.set(BIRTHDAY, dateFormat.parse("1976-06-07"));
		//			person1.set(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 06:06:06"));
		//			person1.set(HEIGHT, 180);
		//			person1.set(ACTIVE, true);
		//			person1.set(COUNTRY, "US");
		//			person1.set(AUTHOR_OF, newArrayList(book1));
		//			person1.set(ACCOUNT_BALANCE, 299.99);
		//			person1.set(SERIAL_NUMBER, 374278348334L);
		//			person1.set(QUOTE, "Computer says no");
		//			person1.set(NUMBER, newArrayList(number1, number2, number3));
		//			personsRepository.add(person1);
		//
		//			person2 = new DefaultEntity(personEMD, dataService);
		//			person2.set(ID, "person2");
		//			person2.set(EMAIL, "bar@localhost");
		//			person2.set(FIRST_NAME, "jane");
		//			person2.set(LAST_NAME, "doe");
		//			person2.set(BIRTHDAY, dateFormat.parse("1980-06-07"));
		//			person2.set(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 07:07:07"));
		//			person2.set(HEIGHT, 165);
		//			person2.set(ACTIVE, false);
		//			person2.set(COUNTRY, "US");
		//			person2.set(AUTHOR_OF, newArrayList(book1, book2));
		//			person2.set(ACCOUNT_BALANCE, -0.70);
		//			person2.set(SERIAL_NUMBER, 67986789879L);
		//			person2.set(QUOTE, "To iterate is human, to recurse divine.");
		//			person2.set(NUMBER, newArrayList(number3, number4));
		//			personsRepository.add(person2);
		//
		//			person3 = new DefaultEntity(personEMD, dataService);
		//			person3.set(ID, "person3");
		//			person3.set(EMAIL, "donald@localhost");
		//			person3.set(FIRST_NAME, "donald");
		//			person3.set(LAST_NAME, "duck");
		//			person3.set(BIRTHDAY, dateFormat.parse("1950-01-31"));
		//			person3.set(BIRTH_TIME, dateTimeFormat.parse("1976-06-07 08:08:08"));
		//			person3.set(HEIGHT, 180);
		//			person3.set(ACTIVE, true);
		//			person3.set(COUNTRY, "NL");
		//			person3.set(AUTHOR_OF, null);
		//			person3.set(ACCOUNT_BALANCE, 1000.00);
		//			person3.set(SERIAL_NUMBER, 23471900909L);
		//			person3.set(QUOTE, "If you're wrong about a boolean, you're only off by a bit");
		//			person3.set(NUMBER, null);
		//			personsRepository.add(person3);
		//		}
		//		catch (ParseException e)
		//		{
		//			throw new RuntimeException(e);
		//		}
	}

	protected abstract void testInt();

	protected abstract void testDecimal();

	protected abstract void testLong();

	protected abstract void testString();

	protected abstract void testDate() throws ParseException;

	protected abstract void testDateTime() throws ParseException;

	protected abstract void testBool();

	protected abstract void testMref();

	protected abstract void testXref();
}