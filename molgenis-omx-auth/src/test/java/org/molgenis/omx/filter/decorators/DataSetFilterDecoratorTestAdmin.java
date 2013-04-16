package org.molgenis.omx.filter.decorators;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.security.Login;
import org.molgenis.io.TupleWriter;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.omx.filter.DataSetFilter;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataSetFilterDecoratorTestAdmin
{
	private MolgenisUser user;
	private DataSetFilterDecorator<DataSetFilter> decorator;
	private final QueryRule[] rules = new QueryRule[0];
	private MolgenisUserService userService;
	private Mapper mapper;
	private QueryRule[] expectedRules;
	private TupleWriter writer;
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
		when(mapper.findById(123)).thenReturn(ownFilter);
		when(mapper.findById(456)).thenReturn(otherFilter);
		decorator = new DataSetFilterDecorator(mapper);
		Login login = mock(Login.class);
		Database database = mock(Database.class);
		userService = mock(MolgenisUserService.class);
		
		when(mapper.getDatabase()).thenReturn(database);
		when(mapper.findByExample(ownFilter)).thenReturn(allEntities);
		when(database.getLogin()).thenReturn(login);
		when(login.getUserId()).thenReturn(1);
		when(userService.findById(1)).thenReturn(user);
		when(user.getSuperuser()).thenReturn(true);
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
	public void find() throws DatabaseException
	{
		decorator.find(rules);
		verify(mapper).find(rules);
	}

	@Test
	public void count() throws DatabaseException
	{
		decorator.find(rules);
		verify(mapper).find(rules);
	}

	@Test
	public void findWithWriter() throws DatabaseException
	{
		decorator.find(writer, rules);
		verify(mapper).find(writer, rules);
	}

	@Test
	public void createFindSqlInclRules() throws DatabaseException
	{
		decorator.createFindSqlInclRules(rules);
		verify(mapper).createFindSqlInclRules(rules);
	}

	@Test
	public void findWithExport() throws DatabaseException
	{
		decorator.find(writer, fieldsToExport, rules);
		verify(mapper).find(writer, fieldsToExport, rules);
	}

	@Test
	public void ownFindByExample() throws DatabaseException
	{
		List<DataSetFilter> results = decorator.findByExample(ownFilter);
		Assert.assertEquals(results, allEntities);	
	}
	
	//read, update, delete own entities, no exceptions expected
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
	
	//update and delete entities owned by another person, exceptions expected
	@Test()
	public void updateOther() throws DatabaseException
	{
		decorator.update(otherEntities);		
	}
	
	@Test()
	public void removeOther() throws DatabaseException
	{
		decorator.remove(otherEntities);		
	}
	
	@Test()
	public void otherFindById() throws DatabaseException
	{
		decorator.findById(456);
	}
}
