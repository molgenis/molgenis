package org.molgenis.ui.security;

import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.FilterInvocation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;
import static org.testng.Assert.assertEquals;

public class MolgenisAccessDecisionVoterTest
{
	@BeforeMethod
	public void setUp()
	{
		MolgenisPermissionService molgenisPermissionService = mock(MolgenisPermissionService.class);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugingranted", Permission.READ)).thenReturn(true);
		when(molgenisPermissionService.hasPermissionOnPlugin("plugindenied", Permission.READ)).thenReturn(false);

		MolgenisUi molgenisUi = mock(MolgenisUi.class);
		MolgenisUiMenu menu = mock(MolgenisUiMenu.class);
		when(molgenisUi.getMenu("menugranted")).thenReturn(menu);
		when(molgenisUi.getMenu("menudenied")).thenReturn(null);

		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBean(MolgenisPermissionService.class)).thenReturn(molgenisPermissionService);
		when(ctx.getBean(MolgenisUi.class)).thenReturn(molgenisUi);

		new ApplicationContextProvider().setApplicationContext(ctx);
	}

	@Test
	public void vote_noPluginNoMenu()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"asdasdsaddas").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_pluginGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/plugin/plugingranted").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_pluginDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/plugin/plugindenied").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_menuPluginGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugingranted").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_menuPluginDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugindenied").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_menuPluginSlashGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugingranted/").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_menuPluginSlashDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugindenied/").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_menuPluginWithPathGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugingranted/path").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_menuPluginWithPathDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugindenied/path").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_menuPluginWithParamsGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugingranted?key=val").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_menuPluginWithParamsDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menuid/plugindenied?key=val").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_menuGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menugranted").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_menuDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menudenied").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}

	@Test
	public void vote_menuSlashGranted()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menugranted/").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
	}

	@Test
	public void vote_menuSlashDenied()
	{
		FilterInvocation filterInvocation = when(mock(FilterInvocation.class).getRequestUrl()).thenReturn(
				"/menu/menudenied/").getMock();
		MolgenisAccessDecisionVoter voter = new MolgenisAccessDecisionVoter();
		assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
	}
}
