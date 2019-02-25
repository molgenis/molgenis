package org.molgenis.core.ui.style;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BootstrapThemePopulatorTest {

  private BootstrapThemePopulator bootstrapThemePopulator;
  private MockitoSession mockitoSession;

  @Mock private StyleService styleService;
  @Mock private DataService dataService;

  @SuppressWarnings("unchecked")
  @Mock
  private Repository<Entity> styleRepository = (Repository<Entity>) mock(Repository.class);

  @Mock private StyleSheet mockSheet = mock(StyleSheet.class);

  @BeforeMethod
  public void initMocks() {
    mockitoSession = mockitoSession().initMocks(this).strictness(Strictness.LENIENT).startMocking();
  }

  @AfterMethod
  public void tearDownAfterMethod() {
    mockitoSession.finishMocking();
  }

  @BeforeMethod
  public void setUpBeforeMethod() {
    bootstrapThemePopulator = new BootstrapThemePopulator(styleService, dataService);
    when(styleRepository.findOneById(ArgumentMatchers.anyString())).thenReturn(mockSheet);
    when(dataService.getRepository(StyleSheetMetadata.STYLE_SHEET)).thenReturn(styleRepository);
  }

  @Test
  public void noNewThemes() throws MolgenisStyleException {
    bootstrapThemePopulator.populate();
    verify(styleService, never()).addStyle(any(), any(), any(), any(), any());
  }

  @Test
  public void addNewTheme() throws MolgenisStyleException {
    when(styleRepository.findOneById(ArgumentMatchers.eq("bootstrap-yeti.min.css")))
        .thenReturn(null);
    bootstrapThemePopulator.populate();
    verify(styleService, times(1)).addStyle(any(), any(), any(), any(), any());
  }
}
