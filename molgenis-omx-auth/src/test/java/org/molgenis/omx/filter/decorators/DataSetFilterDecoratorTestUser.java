package org.molgenis.omx.filter.decorators;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.filter.DataSetFilter;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataSetFilterDecoratorTestUser
{
	private MolgenisUser user;
	private DataSetFilterDecorator<DataSetFilter> decorator;
	private final QueryRule[] rules = new QueryRule[0];
	private MolgenisUserService userService;
	private Mapper mapper;
	private QueryRule[] expectedRules;
	private TupleWriter writer;
	private TupleReader reader;
	private List<String> fieldsToExport;
	private List<DataSetFilter> allEntities;
	private List<DataSetFilter> ownEntities;
	private List<DataSetFilter> otherEntities;
	private DataSetFilter ownFilter;
	private DataSetFilter otherFilter;

	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		allEntities = new ArrayList<DataSetFilter>();
		ownEntities = new ArrayList<DataSetFilter>();
		otherEntities = new ArrayList<DataSetFilter>();

		user = mock(MolgenisUser.class);
		mapper = mock(Mapper.class);
		decorator = new DataSetFilterDecorator(mapper);
		Login login = mock(Login.class);
		Database database = mock(Database.class);
		userService = mock(MolgenisUserService.class);

		when(mapper.getDatabase()).thenReturn(database);
		when(mapper.add(any(List.class))).thenReturn(2);
		when(mapper.create()).thenReturn(new DataSetFilter());
		when(mapper.createList(2)).thenReturn(Arrays.asList(new DataSetFilter(), new DataSetFilter()));
		when(mapper.findByExample(ownFilter)).thenReturn(allEntities);
		when(mapper.findById(123)).thenReturn(ownFilter);
		when(mapper.findById(456)).thenReturn(otherFilter);
		when(database.getLogin()).thenReturn(login);
		when(login.getUserId()).thenReturn(1);
		when(userService.findById(1)).thenReturn(user);
		when(user.getSuperuser()).thenReturn(false);
		when(user.getId()).thenReturn(1);

		decorator.setMolgenisUserService(userService);
		expectedRules = new QueryRule[1];
		expectedRules[0] = new QueryRule("userId", Operator.EQUALS, 1);
		ownFilter = new DataSetFilter();
		ownFilter.setUserId(1);
		ownEntities.add(ownFilter);
		otherFilter = new DataSetFilter();
		otherFilter.setUserId(2);
		otherEntities.add(otherFilter);
		allEntities.add(ownFilter);
		allEntities.add(otherFilter);
	}

	@Test
	public void add() throws DatabaseException
	{
		DataSetFilter dataSetFilter1 = new DataSetFilter();
		DataSetFilter dataSetFilter2 = new DataSetFilter();
		Assert.assertEquals(decorator.add(Arrays.asList(dataSetFilter1, dataSetFilter2)), 2);
		Assert.assertEquals(dataSetFilter1.getUserId_Id(), Integer.valueOf(1));
		Assert.assertEquals(dataSetFilter2.getUserId_Id(), Integer.valueOf(1));
	}

	@Test
	public void create() throws DatabaseException
	{
		DataSetFilter dataSetFilter = decorator.create();
		Assert.assertEquals(dataSetFilter.getUserId_Id(), Integer.valueOf(1));
	}

	@Test
	public void createList() throws DatabaseException
	{
		List<DataSetFilter> dataSetFilters = decorator.createList(2);
		Assert.assertEquals(dataSetFilters.size(), 2);
		Assert.assertEquals(dataSetFilters.get(0).getUserId_Id(), Integer.valueOf(1));
		Assert.assertEquals(dataSetFilters.get(1).getUserId_Id(), Integer.valueOf(1));
	}

	@Test
	public void find() throws DatabaseException
	{
		decorator.find(rules);
		verify(mapper).find(expectedRules);
	}

	@Test
	public void count() throws DatabaseException
	{
		decorator.count(rules);
		verify(mapper).count(expectedRules);
	}

	@Test
	public void findWithWriter() throws DatabaseException
	{
		decorator.find(writer, rules);
		verify(mapper).find(writer, expectedRules);
	}

	@Test
	public void createFindSqlInclRules() throws DatabaseException
	{
		decorator.createFindSqlInclRules(rules);
		verify(mapper).createFindSqlInclRules(expectedRules);
	}

	@Test
	public void findWithExport() throws DatabaseException
	{
		decorator.find(writer, fieldsToExport, rules);
		verify(mapper).find(writer, fieldsToExport, expectedRules);
	}

	@Test
	public void ownFindByExample() throws DatabaseException
	{
		List<DataSetFilter> results = decorator.findByExample(ownFilter);
		Assert.assertEquals(results, ownEntities);
	}

	// read, update, delete own entities, no exceptions expected
	@Test
	public void updateOwn() throws DatabaseException
	{
		decorator.update(ownEntities);
	}

	@Test
	public void removeOwn() throws DatabaseException
	{
		decorator.remove(ownEntities);
	}

	@Test
	public void ownFindById() throws DatabaseException
	{
		decorator.findById(123);
	}

	// update and delete entities owned by another person, exceptions expected
	@Test(expectedExceptions = DatabaseException.class)
	public void updateOther() throws DatabaseException
	{
		decorator.update(otherEntities);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void removeOther() throws DatabaseException
	{
		decorator.remove(otherEntities);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void otherFindById() throws DatabaseException
	{
		decorator.findById(456);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void tupleUpdate() throws DatabaseException
	{

		decorator.update(reader);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void tupleRemove() throws DatabaseException
	{
		decorator.remove(reader);
	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void tupleToList() throws DatabaseException
	{
		decorator.toList(reader, -1);
	}
}
