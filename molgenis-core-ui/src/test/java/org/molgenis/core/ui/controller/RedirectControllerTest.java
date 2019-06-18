package org.molgenis.core.ui.controller;

import static org.mockito.Mockito.when;
import static org.molgenis.core.ui.controller.RedirectController.ID;
import static org.molgenis.core.ui.controller.RedirectController.nodeMatches;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.springframework.web.servlet.view.RedirectView;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RedirectControllerTest extends AbstractMockitoTest {

  private RedirectController redirectController;

  private MenuItem item = MenuItem.create(ID, "Procrastinate!", "url=https://nu.nl/?a%3Db%26c%3Dd");
  private MenuItem userManual =
      MenuItem.create(
          ID,
          "User Manual",
          "url\u003dhttps://drive.google.com/file/d/abcde/view?usp\u003dsharing");

  Menu menu = Menu.create("main", "Main", ImmutableList.of(item, userManual));

  @Mock private MenuReaderService menuReaderService;

  @BeforeMethod
  public void beforeMethod() {
    redirectController = new RedirectController(menuReaderService);
  }

  @Test
  public void testNodeMatchesUrlEncoded() {
    assertTrue(nodeMatches(item, "https://nu.nl/?a=b&c=d"));
  }

  @Test
  public void testNodeMatchesEscapeEqualsCharacter() {
    assertTrue(nodeMatches(userManual, "https://drive.google.com/file/d/abcde/view?usp=sharing"));
  }

  @Test
  public void testNodeMatchesWrongID() {
    assertFalse(nodeMatches(MenuItem.create("wrong-id", "Wrong ID"), "https://nu.nl/"));
  }

  @Test
  public void testNodeMatchesNotAMenuItem() {
    assertFalse(nodeMatches(menu, "https://nu.nl/"));
  }

  @Test(expectedExceptions = AccessDeniedException.class)
  public void testRedirectToUnknownSite() throws AccessDeniedException {
    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));
    redirectController.redirect("https://rogue.com/");
  }

  @Test
  public void testCorrectRedirect() throws AccessDeniedException {
    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));
    String url = "https://nu.nl/?a=b&c=d";
    RedirectView response = (RedirectView) redirectController.redirect(url);
    assertEquals(response.getUrl(), url);
  }
}
