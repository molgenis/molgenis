package org.molgenis.app.manager.decorator;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AppRepositoryDecoratorTest {
  private AppRepositoryDecorator appRepositoryDecorator;
  @Mock private AppManagerService appManagerService;
  @Mock private Repository<App> repository;

  @BeforeMethod
  public void setUpBeforeMethod() {
    initMocks(this);
    appRepositoryDecorator = new AppRepositoryDecorator(repository, appManagerService);
  }

  @Test
  public void testDelete() {
    App app = mock(App.class);
    appRepositoryDecorator.delete(app);
  }

  @Test
  public void testDeleteById() {
    appRepositoryDecorator.deleteById("app");
    verify(appManagerService).deleteApp("app");
    verify(repository).deleteById("app");
  }

  @Test
  public void testDeleteAll() {
    App app1 = mock(App.class);
    when(app1.getId()).thenReturn("app1");
    App app2 = mock(App.class);
    when(app2.getId()).thenReturn("app2");
    Stream<App> apps = Stream.of(app1, app2);

    when(repository.findAll(new QueryImpl<>())).thenReturn(apps);
    appRepositoryDecorator.deleteAll();

    verify(appManagerService).deleteApp("app1");
    verify(repository).deleteById("app1");

    verify(appManagerService).deleteApp("app2");
    verify(repository).deleteById("app2");
  }

  @Test
  public void testDelegate() {
    assertEquals(appRepositoryDecorator.delegate(), repository);
  }

  @Test
  public void testUpdateActivated() {
    App current = mock(App.class);
    when(current.getId()).thenReturn("app");
    when(current.isActive()).thenReturn(false);
    when(repository.findOneById("app")).thenReturn(current);
    App app = mock(App.class);
    when(app.getId()).thenReturn("app");
    when(app.isActive()).thenReturn(true);

    appRepositoryDecorator.update(app);

    verify(appManagerService).activateApp(app);
    verify(repository).update(app);
  }

  @Test
  public void testUpdateDeactivated() {
    App current = mock(App.class);
    when(current.getId()).thenReturn("app");
    when(current.isActive()).thenReturn(true);
    when(repository.findOneById("app")).thenReturn(current);
    App app = mock(App.class);
    when(app.getId()).thenReturn("app");
    when(app.isActive()).thenReturn(false);

    appRepositoryDecorator.update(app);

    verify(appManagerService).deactivateApp(app);
    verify(repository).update(app);
  }

  @Test
  public void testUpdateNoActivationChange() {
    App current = mock(App.class);
    when(current.getId()).thenReturn("app");
    when(current.isActive()).thenReturn(true);
    when(repository.findOneById("app")).thenReturn(current);
    App app = mock(App.class);
    when(app.getId()).thenReturn("app");
    when(app.isActive()).thenReturn(true);

    appRepositoryDecorator.update(app);

    verifyNoMoreInteractions(appManagerService);
    verify(repository).update(app);
  }

  @Test
  public void testUpdate1() {
    App app1 = mock(App.class);
    App app2 = mock(App.class);
    Stream<App> apps = Stream.of(app1, app2);
    appRepositoryDecorator.update(apps);
    verify(repository).update(app1);
    verify(repository).update(app2);
  }
}
