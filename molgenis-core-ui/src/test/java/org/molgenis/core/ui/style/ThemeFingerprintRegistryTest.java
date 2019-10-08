package org.molgenis.core.ui.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

class ThemeFingerprintRegistryTest {
  private StyleService styleService;
  private ThemeFingerprintRegistry themeFingerprintRegistry;

  @BeforeEach
  void setUp() {
    styleService = mock(StyleService.class);
    themeFingerprintRegistry = new ThemeFingerprintRegistry(styleService);
  }

  @Test
  void getFingerprint() throws IOException, MolgenisStyleException {
    String theme = "bootstrap-theme-name.min.css";
    String version = "bootstrap-3";
    String themeUri = "css/theme/" + version + "/" + theme;
    FileSystemResource themeFile = mock(FileSystemResource.class);
    InputStream themeDataStream = IOUtils.toInputStream("yo yo yo data");
    when(themeFile.getInputStream()).thenReturn(themeDataStream);
    when(styleService.getThemeData(theme, BootstrapVersion.BOOTSTRAP_VERSION_3))
        .thenReturn(themeFile);

    // first call
    String firstResult = themeFingerprintRegistry.getFingerprint(themeUri);

    assertNotNull(firstResult);
    verify(styleService).getThemeData(theme, BootstrapVersion.BOOTSTRAP_VERSION_3);

    // second call
    String secondResult = themeFingerprintRegistry.getFingerprint(themeUri);
    verifyNoMoreInteractions(styleService);

    // verify stable key
    assertEquals(secondResult, firstResult);
  }
}
