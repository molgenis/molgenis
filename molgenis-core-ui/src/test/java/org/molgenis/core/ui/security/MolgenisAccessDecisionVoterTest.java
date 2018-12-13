package org.molgenis.core.ui.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.FilterInvocation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisAccessDecisionVoterTest extends AbstractMockitoTest {
  @Mock MenuReaderService menuReaderService;

  private MolgenisAccessDecisionVoter voter;

  @BeforeMethod
  public void setUp() {
    UserPermissionEvaluator permissionService = mock(UserPermissionEvaluator.class);
    when(permissionService.hasPermission(new PluginIdentity("plugingranted"), VIEW_PLUGIN))
        .thenReturn(true);
    when(permissionService.hasPermission(new PluginIdentity("plugindenied"), VIEW_PLUGIN))
        .thenReturn(false);

    ApplicationContext ctx = mock(ApplicationContext.class);
    when(ctx.getBean(UserPermissionEvaluator.class)).thenReturn(permissionService);

    voter = new MolgenisAccessDecisionVoter();

    new ApplicationContextProvider().setApplicationContext(ctx);
  }

  @Test
  public void vote_noPluginNoMenu() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl()).thenReturn("asdasdsaddas").getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_pluginGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/plugin/plugingranted")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_pluginDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/plugin/plugindenied")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_menuPluginGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_menuPluginDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_menuPluginSlashGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted/")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_menuPluginSlashDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied/")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_menuPluginWithPathGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted/path")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_menuPluginWithPathDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied/path")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_menuPluginWithParamsGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted?key=val")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_menuPluginWithParamsDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied?key=val")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_menuGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menugranted")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_menuDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl()).thenReturn("/menu/menudenied").getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }

  @Test
  public void vote_menuSlashGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menugranted/")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_GRANTED);
  }

  @Test
  public void vote_menuSlashDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menudenied/")
            .getMock();
    assertEquals(voter.vote(null, filterInvocation, null), ACCESS_DENIED);
  }
}
