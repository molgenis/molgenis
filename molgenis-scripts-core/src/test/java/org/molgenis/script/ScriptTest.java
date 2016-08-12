package org.molgenis.script;

import org.molgenis.file.FileStore;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class ScriptTest
{
	@Test
	public void generateScript() throws IOException
	{
		FileStore fileStore = new FileStore(System.getProperty("java.io.tmpdir"));
		Map<String, Object> parameterValues = Collections.singletonMap("name", "Piet");

		Script script = mock(Script.class);
		when(script.getContent()).thenReturn("Hey ${name}");
		when(script.generateScript(fileStore, "txt", parameterValues)).thenCallRealMethod();

		File f = script.generateScript(fileStore, "txt", parameterValues);
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.getPath().endsWith(".txt"));
		String s = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(f)));
		assertEquals("Hey Piet", s);
		f.delete();
	}
}
