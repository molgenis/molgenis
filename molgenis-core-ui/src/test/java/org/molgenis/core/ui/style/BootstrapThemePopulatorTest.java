package org.molgenis.core.ui.style;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.core.ui.style.exception.GetThemeException;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

class BootstrapThemePopulatorTest {

  private BootstrapThemePopulator bootstrapThemePopulator;
  private MockitoSession mockitoSession;

  @Mock private StyleService styleService;
  @Mock private DataService dataService;

  @SuppressWarnings("unchecked")
  @Mock
  private Repository<Entity> styleRepository = (Repository<Entity>) mock(Repository.class);

  @Mock private StyleSheet mockSheet = mock(StyleSheet.class);

  @BeforeEach
  void initMocks() {
    mockitoSession = mockitoSession().initMocks(this).strictness(Strictness.LENIENT).startMocking();
  }

  @AfterEach
  void tearDownAfterMethod() {
    mockitoSession.finishMocking();
  }

  @BeforeEach
  void setUpBeforeMethod() {
    bootstrapThemePopulator = new BootstrapThemePopulator(styleService, dataService);
    when(styleRepository.findOneById(ArgumentMatchers.anyString())).thenReturn(mockSheet);
    when(dataService.getRepository(StyleSheetMetadata.STYLE_SHEET)).thenReturn(styleRepository);
  }

  @Test
  void noNewThemes() throws GetThemeException {
    bootstrapThemePopulator.populate();
    verify(styleService, never()).addStyle(any(), any(), any(), any(), any());
  }

  @Test
  void addNewTheme() throws GetThemeException {
    when(styleRepository.findOneById(ArgumentMatchers.eq("bootstrap-yeti.min.css")))
        .thenReturn(null);
    bootstrapThemePopulator.populate();
    verify(styleService, times(1)).addStyle(any(), any(), any(), any(), any());
  }
}
