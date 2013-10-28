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
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.HandleRequestDelegationException;
import org.springframework.context.ApplicationContext;
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
	private List<StudyDataRequest> allEntities;
	private List<StudyDataRequest> adminEntities;
	private List<StudyDataRequest> userEntities;
	private StudyDataRequest adminStudyDataRequest;
	private StudyDataRequest userStudyDataRequest;

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
		Database database = mock(Database.class);
		userService = mock(MolgenisUserService.class);

		when(mapper.getDatabase()).thenReturn(database);
		when(userService.findById(1)).thenReturn(admin);
		when(admin.getSuperuser()).thenReturn(true);
		when(admin.getId()).thenReturn(1);
		when(userService.findById(2)).thenReturn(user);
		when(user.getSuperuser()).thenReturn(false);
		when(user.getId()).thenReturn(2);

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

		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBean(org.molgenis.security.user.MolgenisUserService.class)).thenReturn(userService);
		new ApplicationContextProvider().setApplicationContext(ctx);
	}

	@Test
	public void findAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);
		// when(userService.getCurrentUser()).thenReturn(admin);

		decorator.find(initialRules);
		verify(mapper).find(initialRules);
	}

	@Test
	public void find() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.find(initialRules);
		verify(mapper).find(expectedUserRules);
	}

	@Test
	public void countAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.find(initialRules);
		verify(mapper).find(initialRules);
	}

	@Test
	public void count() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.find(initialRules);
		verify(mapper).find(expectedUserRules);
	}

	@Test
	public void createFindSqlInclRulesAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.createFindSqlInclRules(initialRules);
		verify(mapper).createFindSqlInclRules(initialRules);
	}

	@Test
	public void createFindSqlInclRules() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.createFindSqlInclRules(initialRules);
		verify(mapper).createFindSqlInclRules(expectedUserRules);
	}

	// read, update, delete own entities, no exceptions expected
	@Test
	public void updateOwnAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.update(adminEntities);
	}

	@Test
	public void updateOwn() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.update(userEntities);
	}

	@Test
	public void removeOwnAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.remove(adminEntities);
	}

	@Test
	public void removeOwn() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.remove(userEntities);
	}

	@Test
	public void ownFindByIdAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.findById(123);
	}

	@Test
	public void ownFindById() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.findById(456);
	}

	@Test()
	public void updateOtherAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.update(userEntities);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void updateOther() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.update(adminEntities);
	}

	@Test()
	public void removeOtherAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.remove(userEntities);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void removeOther() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.remove(adminEntities);
	}

	@Test()
	public void otherFindByIdAdmin() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(admin);

		decorator.findById(456);
	}

	@Test(expectedExceptions = DatabaseException.class)
	public void otherFindById() throws DatabaseException
	{
		when(userService.getCurrentUser()).thenReturn(user);

		decorator.findById(123);
	}

}