package org.molgenis.script;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.script.core.GenerateScriptException;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptOutputHandler;
import org.molgenis.script.core.ScriptParameter;
import org.molgenis.script.core.ScriptRunner;
import org.molgenis.script.core.ScriptRunnerFactory;
import org.molgenis.script.core.ScriptType;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.test.AbstractMockitoTest;

class SavedScriptRunnerTest extends AbstractMockitoTest {

  @Mock private ScriptRunnerFactory scriptRunnerFactory;
  @Mock private DataService dataService;
  @Mock private FileStore fileStore;
  @Mock private TokenService tokenService;
  @Mock private FileMetaFactory fileMetaFactory;
  private SavedScriptRunner savedScriptRunner;

  @BeforeEach
  void setUpBeforeMethod() {
    savedScriptRunner =
        new SavedScriptRunner(
            scriptRunnerFactory, dataService, fileStore, tokenService, fileMetaFactory);
  }

  @Test
  void testRunScript() {
    String scriptTypeName = "MyScriptType";
    ScriptType scriptType =
        when(mock(ScriptType.class).getName()).thenReturn(scriptTypeName).getMock();
    var scriptOutputHandler = mock(ScriptOutputHandler.class);

    String scriptName = "MyScript";
    Script script = mock(Script.class);
    when(script.getParameters()).thenReturn(emptyList());
    when(script.getScriptType()).thenReturn(scriptType);

    @SuppressWarnings("unchecked")
    Query<Script> query = mock(Query.class, RETURNS_SELF);
    when(query.eq("name", scriptName).findOne()).thenReturn(script);
    when(dataService.query("sys_scr_Script", Script.class)).thenReturn(query);

    ScriptRunner scriptRunner = mock(ScriptRunner.class);

    when(scriptRunnerFactory.getScriptRunner(scriptTypeName)).thenReturn(scriptRunner);
    savedScriptRunner.runScript(scriptName, emptyMap(), scriptOutputHandler);

    verify(scriptRunner).runScript(script, emptyMap(), scriptOutputHandler);
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Test
  void testRunScriptOutputFile() throws IOException {
    String scriptTypeName = "MyScriptType";
    ScriptType scriptType =
        when(mock(ScriptType.class).getName()).thenReturn(scriptTypeName).getMock();
    var scriptOutputHandler = mock(ScriptOutputHandler.class);

    String scriptName = "MyScript";
    Script script = mock(Script.class);
    when(script.getParameters()).thenReturn(emptyList());
    when(script.getScriptType()).thenReturn(scriptType);

    @SuppressWarnings("unchecked")
    Query<Script> query = mock(Query.class, RETURNS_SELF);
    when(query.eq("name", scriptName).findOne()).thenReturn(script);
    when(dataService.query("sys_scr_Script", Script.class)).thenReturn(query);

    ScriptRunner scriptRunner = mock(ScriptRunner.class);
    when(scriptRunner.hasFileOutput(script)).thenReturn(true);
    when(scriptRunnerFactory.getScriptRunner(scriptTypeName)).thenReturn(scriptRunner);

    FileMeta fileMeta = mock(FileMeta.class);
    when(fileMetaFactory.create(any(String.class))).thenReturn(fileMeta);

    File savedScriptRunnerTest = File.createTempFile("SavedScriptRunnerTest", null);
    try {
      when(fileStore.getFileUnchecked(any())).thenReturn(savedScriptRunnerTest);
      savedScriptRunner.runScript(scriptName, emptyMap(), scriptOutputHandler);
    } finally {
      savedScriptRunnerTest.delete();
    }
    verify(dataService).add("sys_FileMeta", fileMeta);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
    verify(scriptRunner).runScript(eq(script), paramsCaptor.capture(), eq(scriptOutputHandler));
    assertTrue(paramsCaptor.getValue().containsKey("outputFile"));
  }

  @SuppressWarnings("deprecation")
  @Test
  void testRunScriptMissingParameterValues() {
    String scriptName = "MyScript";
    var scriptOutputHandler = mock(ScriptOutputHandler.class);

    ScriptParameter scriptParameter =
        when(mock(ScriptParameter.class).getName()).thenReturn("param0").getMock();

    Script script = mock(Script.class);
    when(script.getParameters()).thenReturn(singletonList(scriptParameter));

    @SuppressWarnings("unchecked")
    Query<Script> query = mock(Query.class, RETURNS_SELF);
    when(query.eq("name", scriptName).findOne()).thenReturn(script);
    when(dataService.query("sys_scr_Script", Script.class)).thenReturn(query);

    assertThrows(
        GenerateScriptException.class,
        () -> savedScriptRunner.runScript(scriptName, emptyMap(), scriptOutputHandler));
  }

  @Test
  void testRunScriptUnknownScript() {
    String scriptName = "MyScript";
    var scriptOutputHandler = mock(ScriptOutputHandler.class);

    @SuppressWarnings("unchecked")
    Query<Script> query = mock(Query.class, RETURNS_SELF);
    when(query.eq("name", scriptName).findOne()).thenReturn(null);
    when(dataService.query("sys_scr_Script", Script.class)).thenReturn(query);

    assertThrows(
        UnknownEntityException.class,
        () -> savedScriptRunner.runScript(scriptName, emptyMap(), scriptOutputHandler));
  }
}
