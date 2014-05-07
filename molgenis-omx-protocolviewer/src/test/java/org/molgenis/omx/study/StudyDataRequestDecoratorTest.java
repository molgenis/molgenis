package org.molgenis.omx.study;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.CrudRepositoryDecorator;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class StudyDataRequestDecoratorTest
{
	private static final String USERNAME_USER = "user";
	private static final String USERNAME_ADMIN = "admin";

	private static Authentication AUTHENTICATION_PREVIOUS;
	private Authentication authentication;

	private StudyDataRequestDecorator studyDataRequestDecorator;
	private CrudRepositoryDecorator crudRepositoryDecorator;
	private List<QueryRule> adminRules, userRules;
	private MolgenisUser userUser, adminUser;
	private StudyDataRequest userStudyDataRequest, adminStudyDataRequest;
	private QueryRule exampleRule;
	private Object exampleUserId, exampleAdminId;
	private List<Object> exampleUserIds, exampleAdminIds, exampleUserAdminIds;

	@BeforeClass
	public void setUpBeforeClass()
	{
		AUTHENTICATION_PREVIOUS = SecurityContextHolder.getContext().getAuthentication();
		authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_PREVIOUS);
	}

	@BeforeMethod
	public void setUp()
	{
		crudRepositoryDecorator = mock(CrudRepositoryDecorator.class);
		studyDataRequestDecorator = new StudyDataRequestDecorator(crudRepositoryDecorator);

		userUser = when(mock(MolgenisUser.class).getUsername()).thenReturn(USERNAME_USER).getMock();
		when(userUser.getId()).thenReturn(0);
		when(userUser.getSuperuser()).thenReturn(false);
		adminUser = when(mock(MolgenisUser.class).getUsername()).thenReturn(USERNAME_ADMIN).getMock();
		when(adminUser.getId()).thenReturn(1);
		when(adminUser.getSuperuser()).thenReturn(true);

		userRules = Arrays.asList(new QueryRule(StudyDataRequest.MOLGENISUSER, Operator.EQUALS, userUser));
		adminRules = Collections.<QueryRule> emptyList();

		MolgenisUserService userService = mock(MolgenisUserService.class);
		when(userService.getUser(USERNAME_USER)).thenReturn(userUser);
		when(userService.getUser(USERNAME_ADMIN)).thenReturn(adminUser);
		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBean(org.molgenis.security.user.MolgenisUserService.class)).thenReturn(userService);
		new ApplicationContextProvider().setApplicationContext(ctx);

		userStudyDataRequest = when(mock(StudyDataRequest.class).getMolgenisUser()).thenReturn(userUser).getMock();
		adminStudyDataRequest = when(mock(StudyDataRequest.class).getMolgenisUser()).thenReturn(adminUser).getMock();

		exampleRule = new QueryRule("field", Operator.EQUALS, "value");
		exampleUserId = 0;
		exampleUserIds = Arrays.asList(exampleUserId);
		exampleAdminId = 1;
		exampleAdminIds = Arrays.asList(exampleAdminId);
		exampleUserAdminIds = Arrays.asList(exampleUserId, exampleAdminId);

		when(crudRepositoryDecorator.findOne(exampleUserId)).thenReturn(userStudyDataRequest);
		when(crudRepositoryDecorator.findOne(exampleAdminId)).thenReturn(adminStudyDataRequest);

		when(crudRepositoryDecorator.findAll(exampleUserIds)).thenReturn(
				Lists.<Entity> newArrayList(userStudyDataRequest));
		when(crudRepositoryDecorator.findAll(exampleAdminIds)).thenReturn(
				Lists.<Entity> newArrayList(adminStudyDataRequest));
		when(crudRepositoryDecorator.findAll(exampleUserAdminIds)).thenReturn(
				Arrays.<Entity> asList(userStudyDataRequest, adminStudyDataRequest));
	}

	@SuppressWarnings("resource")
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void StudyDataRequestDecorator()
	{
		new StudyDataRequestDecorator(null);
	}

	@Test
	public void count_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.count();
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).count(argument.capture());
		assertEquals(argument.getValue().getRules(), userRules);
	}

	@Test
	public void count_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.count();
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).count(argument.capture());
		assertEquals(argument.getValue().getRules(), adminRules);
	}

	@Test
	public void countQuery_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.count(new QueryImpl(exampleRule));
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).count(argument.capture());
		assertEquals(argument.getValue().getRules(), createList(exampleRule, userRules));
	}

	@Test
	public void countQuery_Admin()
	{
		QueryRule initialRule = new QueryRule("field", Operator.EQUALS, "value");
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.count(new QueryImpl(initialRule));
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).count(argument.capture());
		assertEquals(argument.getValue().getRules(), createList(initialRule, adminRules));
	}

	@Test
	public void add_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.add(userStudyDataRequest);
		verify(crudRepositoryDecorator).add(userStudyDataRequest);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void add_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.add(adminStudyDataRequest);
	}

	@Test
	public void add_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.add(adminStudyDataRequest);
		verify(crudRepositoryDecorator).add(adminStudyDataRequest);
	}

	@Test
	public void add_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.add(userStudyDataRequest);
		verify(crudRepositoryDecorator).add(userStudyDataRequest);
	}

	@Test
	public void addIterable_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.add(Collections.singletonList(userStudyDataRequest));
		verify(crudRepositoryDecorator).add(Collections.singletonList(userStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void addIterable_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.add(Collections.singletonList(adminStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void addIterable_UserNotAllowedSome()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.add(Arrays.asList(userStudyDataRequest, adminStudyDataRequest));
	}

	@Test
	public void addIterable_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.add(Collections.singletonList(adminStudyDataRequest));
		verify(crudRepositoryDecorator).add(Collections.singletonList(adminStudyDataRequest));
	}

	@Test
	public void addIterable_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.add(Collections.singletonList(userStudyDataRequest));
		verify(crudRepositoryDecorator).add(Collections.singletonList(userStudyDataRequest));
	}

	@Test
	public void update_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(userStudyDataRequest);
		verify(crudRepositoryDecorator).update(userStudyDataRequest);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void update_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(adminStudyDataRequest);
	}

	@Test
	public void update_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.update(adminStudyDataRequest);
		verify(crudRepositoryDecorator).update(adminStudyDataRequest);
	}

	@Test
	public void update_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.update(userStudyDataRequest);
		verify(crudRepositoryDecorator).update(userStudyDataRequest);
	}

	@Test
	public void updateIterable_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(Collections.singletonList(userStudyDataRequest));
		verify(crudRepositoryDecorator).update(Collections.singletonList(userStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateIterable_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(Collections.singletonList(adminStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateIterable_UserNotAllowedSome()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(Arrays.asList(userStudyDataRequest, adminStudyDataRequest));
	}

	@Test
	public void updateIterable_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.update(Collections.singletonList(adminStudyDataRequest));
		verify(crudRepositoryDecorator).update(Collections.singletonList(adminStudyDataRequest));
	}

	@Test
	public void updateIterable_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.update(Collections.singletonList(userStudyDataRequest));
		verify(crudRepositoryDecorator).update(Collections.singletonList(userStudyDataRequest));
	}

	@Test
	public void updateList_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(Collections.singletonList(userStudyDataRequest), DatabaseAction.UPDATE);
		verify(crudRepositoryDecorator).update(Collections.singletonList(userStudyDataRequest), DatabaseAction.UPDATE);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateList_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(Collections.singletonList(adminStudyDataRequest), DatabaseAction.UPDATE);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void updateList_UserNotAllowedSome()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.update(Arrays.asList(userStudyDataRequest, adminStudyDataRequest),
				DatabaseAction.UPDATE);
	}

	@Test
	public void updateList_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.update(Collections.singletonList(adminStudyDataRequest), DatabaseAction.UPDATE);
		verify(crudRepositoryDecorator).update(Collections.singletonList(adminStudyDataRequest), DatabaseAction.UPDATE);
	}

	@Test
	public void updateList_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.update(Collections.singletonList(userStudyDataRequest), DatabaseAction.UPDATE);
		verify(crudRepositoryDecorator).update(Collections.singletonList(userStudyDataRequest), DatabaseAction.UPDATE);
	}

	@Test
	public void findAllQuery_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.findAll(new QueryImpl(exampleRule));
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).findAll(argument.capture());
		assertEquals(argument.getValue().getRules(), createList(exampleRule, userRules));
	}

	@Test
	public void findAllQuery_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.findAll(new QueryImpl(exampleRule));
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).findAll(argument.capture());
		assertEquals(argument.getValue().getRules(), createList(exampleRule, adminRules));
	}

	@Test
	public void findOneQuery_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.findOne(new QueryImpl(exampleRule));
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).findOne(argument.capture());
		assertEquals(argument.getValue().getRules(), createList(exampleRule, userRules));
	}

	@Test
	public void findOneQuery_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.findOne(new QueryImpl(exampleRule));
		ArgumentCaptor<Query> argument = ArgumentCaptor.forClass(Query.class);
		verify(crudRepositoryDecorator).findOne(argument.capture());
		assertEquals(argument.getValue().getRules(), createList(exampleRule, adminRules));
	}

	@Test
	public void findOne_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.findOne(exampleUserId);
		verify(crudRepositoryDecorator).findOne(exampleUserId);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findOne_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.findOne(exampleAdminId);
	}

	@Test
	public void findOne_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.findOne(exampleAdminId);
		verify(crudRepositoryDecorator).findOne(exampleAdminId);
	}

	@Test
	public void findOne_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.findOne(exampleUserId);
		verify(crudRepositoryDecorator).findOne(exampleUserId);
	}

	@Test
	public void findAllIterable_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		Iterable<Entity> entities = studyDataRequestDecorator.findAll(exampleUserIds);
		assertEquals(entities, Arrays.asList(userStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findAllIterable_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.findAll(exampleAdminIds);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void findAllIterable_UserNotAllowedSome()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.findAll(exampleUserAdminIds);
	}

	@Test
	public void findAllIterable_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		Iterable<Entity> entities = studyDataRequestDecorator.findAll(exampleAdminIds);
		assertEquals(entities, Arrays.asList(adminStudyDataRequest));
	}

	@Test
	public void findAllIterable_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		Iterable<Entity> entities = studyDataRequestDecorator.findAll(exampleUserIds);
		assertEquals(entities, Arrays.asList(userStudyDataRequest));
	}

	@Test
	public void delete_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.delete(userStudyDataRequest);
		verify(crudRepositoryDecorator).delete(userStudyDataRequest);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void delete_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.delete(adminStudyDataRequest);
	}

	@Test
	public void delete_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.delete(adminStudyDataRequest);
		verify(crudRepositoryDecorator).delete(adminStudyDataRequest);
	}

	@Test
	public void delete_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.delete(adminStudyDataRequest);
		verify(crudRepositoryDecorator).delete(adminStudyDataRequest);
	}

	@Test
	public void deleteIterable_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.delete(Collections.singletonList(userStudyDataRequest));
		verify(crudRepositoryDecorator).delete(Collections.singletonList(userStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteIterable_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.delete(Collections.singletonList(adminStudyDataRequest));
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteIterable_UserNotAllowedSome()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.delete(Arrays.asList(userStudyDataRequest, adminStudyDataRequest));
	}

	@Test
	public void deleteIterable_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.delete(Collections.singletonList(adminStudyDataRequest));
		verify(crudRepositoryDecorator).delete(Collections.singletonList(adminStudyDataRequest));
	}

	@Test
	public void deleteIterable_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.delete(Collections.singletonList(userStudyDataRequest));
		verify(crudRepositoryDecorator).delete(Collections.singletonList(userStudyDataRequest));
	}

	@Test
	public void deleteById_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.deleteById(exampleUserId);
		verify(crudRepositoryDecorator).deleteById(exampleUserId);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteById_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.deleteById(exampleAdminId);
	}

	@Test
	public void deleteById_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.deleteById(exampleAdminId);
		verify(crudRepositoryDecorator).deleteById(exampleAdminId);
	}

	@Test
	public void deleteById_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.deleteById(exampleUserId);
		verify(crudRepositoryDecorator).deleteById(exampleUserId);
	}

	@Test
	public void deleteByIdIterable_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.deleteById(exampleUserIds);
		verify(crudRepositoryDecorator).deleteById(exampleUserIds);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteByIdIterable_UserNotAllowed()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.deleteById(exampleAdminIds);
	}

	@Test(expectedExceptions = MolgenisDataAccessException.class)
	public void deleteByIdIterable_UserNotAllowedSome()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		studyDataRequestDecorator.deleteById(exampleUserAdminIds);
	}

	@Test
	public void deleteByIdIterable_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.deleteById(exampleAdminIds);
		verify(crudRepositoryDecorator).deleteById(exampleAdminIds);
	}

	@Test
	public void deleteByIdIterable_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		studyDataRequestDecorator.deleteById(exampleUserIds);
		verify(crudRepositoryDecorator).deleteById(exampleUserIds);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void deleteAll_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		when(crudRepositoryDecorator.findAll(new QueryImpl(userRules))).thenReturn(
				Lists.<Entity> newArrayList(userStudyDataRequest));
		studyDataRequestDecorator.deleteAll();
		ArgumentCaptor<Iterable> argument = ArgumentCaptor.forClass(Iterable.class);
		verify(crudRepositoryDecorator).delete(argument.capture());
		assertEquals(argument.getValue(), Collections.singletonList(userStudyDataRequest));
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Test
	public void deleteAll_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		when(crudRepositoryDecorator.findAll(new QueryImpl(adminRules))).thenReturn(
				Lists.<Entity> newArrayList(adminStudyDataRequest));
		studyDataRequestDecorator.deleteAll();
		ArgumentCaptor<Iterable> argument = ArgumentCaptor.forClass(Iterable.class);
		verify(crudRepositoryDecorator).delete(argument.capture());
		assertEquals(argument.getValue(), Collections.singletonList(adminStudyDataRequest));
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Test
	public void deleteAll_AdminAsUser()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		when(crudRepositoryDecorator.findAll(new QueryImpl(adminRules))).thenReturn(
				Lists.<Entity> newArrayList(userStudyDataRequest));
		studyDataRequestDecorator.deleteAll();
		ArgumentCaptor<Iterable> argument = ArgumentCaptor.forClass(Iterable.class);
		verify(crudRepositoryDecorator).delete(argument.capture());
		assertEquals(argument.getValue(), Collections.singletonList(userStudyDataRequest));
	}

	@Test
	public void iterator_User()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_USER);
		when(crudRepositoryDecorator.findAll(new QueryImpl(userRules))).thenReturn(
				Lists.<Entity> newArrayList(userStudyDataRequest));
		assertEquals(Lists.newArrayList(studyDataRequestDecorator.iterator()),
				Collections.singletonList(userStudyDataRequest));
	}

	@Test
	public void iterator_Admin()
	{
		when(authentication.getPrincipal()).thenReturn(USERNAME_ADMIN);
		when(crudRepositoryDecorator.findAll(new QueryImpl(adminRules))).thenReturn(
				Lists.<Entity> newArrayList(adminStudyDataRequest));
		assertEquals(Lists.newArrayList(studyDataRequestDecorator.iterator()),
				Collections.singletonList(adminStudyDataRequest));
	}

	private List<QueryRule> createList(QueryRule queryRule, List<QueryRule> queryRules)
	{
		List<QueryRule> combinedList = new ArrayList<QueryRule>();
		combinedList.add(queryRule);
		combinedList.addAll(queryRules);
		return combinedList;
	}
}
