package org.molgenis.script;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.file.FileStore;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;

public class ScriptTest
{

	@Test
	public void generateScript() throws FileNotFoundException, IOException
	{
		Script script = new Script(mock(DataService.class));
		script.setContent("Hey ${name}");

		FileStore fileStore = new FileStore(System.getProperty("java.io.tmpdir"));
		Map<String, Object> parameterValues = Collections.<String, Object> singletonMap("name", "Piet");
		File f = script.generateScript(fileStore, "txt", parameterValues);
		assertNotNull(f);
		assertTrue(f.exists());
		assertTrue(f.getPath().endsWith(".txt"));
		String s = FileCopyUtils.copyToString(new InputStreamReader(new FileInputStream(f)));
		assertEquals("Hey Piet", s);
		f.delete();
	}
}
