package org.molgenis.app.manager.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;

class AppRepositoryDecoratorTest {
  private AppRepositoryDecorator appRepositoryDecorator;
  @Mock private AppManagerService appManagerService;
  @Mock private Repository<App> repository;

  @BeforeEach
  void setUpBeforeMethod() {
    initMocks(this);
    appRepositoryDecorator = new AppRepositoryDecorator(repository, appManagerService);
  }

  @Test
  void testDelete() {
    String appId = "app";
    App app = when(mock(App.class).getId()).thenReturn(appId).getMock();
    appRepositoryDecorator.delete(app);
    verify(repository).deleteById(appId);
  }

  @Test
  void testDeleteById() {
    appRepositoryDecorator.deleteById("app");
    verify(appManagerService).deleteApp("app");
    verify(repository).deleteById("app");
  }

  @Test
  void testDeleteAll() {
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
  void testDelegate() {
    assertEquals(repository, appRepositoryDecorator.delegate());
  }

  @Test
  void testUpdateActivated() {
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
  void testUpdateDeactivated() {
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
  void testUpdateNoActivationChange() {
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
  void testUpdate1() {
    App app1 = mock(App.class);
    App app2 = mock(App.class);
    Stream<App> apps = Stream.of(app1, app2);
    appRepositoryDecorator.update(apps);
    verify(repository).update(app1);
    verify(repository).update(app2);
  }
}
