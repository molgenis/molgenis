package org.molgenis.core.ui.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.springframework.security.web.FilterInvocation;

@MockitoSettings(strictness = Strictness.LENIENT)
class MolgenisAccessDecisionVoterTest extends AbstractMockitoTest {
  @Mock MenuReaderService menuReaderService;
  @Mock UserPermissionEvaluator userPermissionEvaluator;
  @Mock Menu menu;

  private MolgenisAccessDecisionVoter voter;

  @BeforeEach
  void setUp() {
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new PluginIdentity("plugingranted"), VIEW_PLUGIN);
    doReturn(false)
        .when(userPermissionEvaluator)
        .hasPermission(new PluginIdentity("plugindenied"), VIEW_PLUGIN);

    voter = new MolgenisAccessDecisionVoter();
    voter.setMenuReaderService(menuReaderService);
    voter.setUserPermissionEvaluator(userPermissionEvaluator);

    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));
    when(menu.getPath("menugranted"))
        .thenReturn(Optional.of(ImmutableList.of("menu", "main", "menugranted")));
    when(menu.findMenu("menudenied")).thenReturn(Optional.empty());
  }

  @Test
  void vote_noPluginNoMenu() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl()).thenReturn("asdasdsaddas").getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_pluginGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/plugin/plugingranted")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_pluginDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/plugin/plugindenied")
            .getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied")
            .getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginSlashGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted/")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginSlashDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied/")
            .getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginWithPathGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted/path")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginWithPathDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied/path")
            .getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginWithParamsGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugingranted?key=val")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuPluginWithParamsDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menuid/plugindenied?key=val")
            .getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menugranted")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl()).thenReturn("/menu/menudenied").getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuSlashGranted() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menugranted/")
            .getMock();
    assertEquals(ACCESS_GRANTED, voter.vote(null, filterInvocation, null));
  }

  @Test
  void vote_menuSlashDenied() {
    FilterInvocation filterInvocation =
        when(mock(FilterInvocation.class).getRequestUrl())
            .thenReturn("/menu/menudenied/")
            .getMock();
    assertEquals(ACCESS_DENIED, voter.vote(null, filterInvocation, null));
  }
}
