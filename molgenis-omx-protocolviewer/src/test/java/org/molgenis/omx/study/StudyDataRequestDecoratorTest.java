package org.molgenis.omx.study;

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
import org.molgenis.io.TupleReader;
import org.molgenis.io.TupleWriter;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.service.MolgenisUserService;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StudyDataRequestDecoratorTest
{
	private MolgenisUser admin;
	private MolgenisUser user;
	private StudyDataRequestDecorator<StudyDataRequest> decorator;
	private QueryRule[] initialRules;
	private MolgenisUserService userService;
	private Mapper<StudyDataRequest> mapper;
	private QueryRule[] expectedUserRules;
	private TupleWriter writer;
	private TupleReader reader;
	private List<String> fieldsToExport;
	private List<StudyDataRequest> allEntities;
	private List<StudyDataRequest> adminEntities;
	private List<StudyDataRequest> userEntities;
	private StudyDataRequest adminStudyDataRequest;
	private StudyDataRequest userStudyDataRequest;
	private Login login;

	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		allEntities = new ArrayList<StudyDataRequest>();
		adminEntities = new ArrayList<StudyDataRequest>();
		userEntities = new ArrayList<StudyDataRequest>();

		admin = mock(MolgenisUser.class);
		user = mock(MolgenisUser.class);
		mapper = mock(Mapper.class);
		when(mapper.findById(123)).thenReturn(adminStudyDataRequest);
		when(mapper.findById(456)).thenReturn(userStudyDataRequest);
		decorator = new StudyDataRequestDecorator<StudyDataRequest>(mapper);
		login = mock(Login.class);
		Database database = mock(Database.class);
		userService = mock(MolgenisUserService.class);

		when(mapper.getDatabase()).thenReturn(database);
		when(database.getLogin()).thenReturn(login);
		when(userService.findById(1)).thenReturn(admin);
		when(admin.getSuperuser()).thenReturn(true);
		when(admin.getId()).thenReturn(1);
		when(userService.findById(2)).thenReturn(user);
		when(user.getSuperuser()).thenReturn(false);
		when(user.getId()).thenReturn(2);

		decorator.setMolgenisUserService(userService);
		// initially the set of query rules is empty
		initialRules = new QueryRule[0];
		// for user that are not superusers, a queryrule checking for the user is added
		expectedUserRules = new QueryRule[1];
		expectedUserRules[0] = new QueryRule("MolgenisUser", Operator.EQUALS, user);
		adminStudyDataRequest = new StudyDataRequest();
		adminStudyDataRequest.setMolgenisUser(admin);
		adminEntities.add(adminStudyDataRequest);
		userStudyDataRequest = new StudyDataRequest();
		userStudyDataRequest.setMolgenisUser(user);
		userEntities.add(userStudyDataRequest);
		allEntities.add(adminStudyDataRequest);
		allEntities.add(userStudyDataRequest);
	}

	@Test
	public void findAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.find(initialRules);
		verify(mapper).find(initialRules);
	}

	@Test
	public void find() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.find(initialRules);
		verify(mapper).find(expectedUserRules);
	}

	@Test
	public void countAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.find(initialRules);
		verify(mapper).find(initialRules);
	}

	@Test
	public void count() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.find(initialRules);
		verify(mapper).find(expectedUserRules);
	}

	@Test
	public void findWithWriterAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.find(writer, initialRules);
		verify(mapper).find(writer, initialRules);
	}

	@Test
	public void findWithWriter() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.find(writer, initialRules);
		verify(mapper).find(writer, expectedUserRules);
	}

	@Test
	public void createFindSqlInclRulesAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.createFindSqlInclRules(initialRules);
		verify(mapper).createFindSqlInclRules(initialRules);
	}

	@Test
	public void createFindSqlInclRules() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.createFindSqlInclRules(initialRules);
		verify(mapper).createFindSqlInclRules(expectedUserRules);
	}

	@Test
	public void findWithExportAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.find(writer, fieldsToExport, initialRules);
		verify(mapper).find(writer, fieldsToExport, initialRules);
	}

	@Test
	public void findWithExport() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.find(writer, fieldsToExport, initialRules);
		verify(mapper).find(writer, fieldsToExport, expectedUserRules);
	}

	// read, update, delete own entities, no exceptions expected
	@Test
	public void updateOwnAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.update(adminEntities);
	}

	@Test
	public void updateOwn() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.update(userEntities);
	}

	@Test
	public void removeOwnAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.remove(adminEntities);
	}

	@Test
	public void removeOwn() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.remove(userEntities);
	}

	@Test
	public void ownFindByIdAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.findById(123);
	}

	@Test
	public void ownFindById() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.findById(456);
	}

	@Test()
	public void updateOtherAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.update(userEntities);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void updateOther() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.update(adminEntities);
	}

	@Test()
	public void removeOtherAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.remove(userEntities);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void removeOther() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.remove(adminEntities);
	}

	@Test()
	public void otherFindByIdAdmin() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(1);

		decorator.findById(456);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void otherFindById() throws DatabaseException
	{
		when(login.getUserId()).thenReturn(2);

		decorator.findById(123);
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