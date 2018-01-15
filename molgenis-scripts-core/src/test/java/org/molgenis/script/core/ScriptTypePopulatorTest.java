package org.molgenis.script.core;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.script.core.ScriptTypeMetaData.SCRIPT_TYPE;
import static org.testng.Assert.assertEquals;

public class ScriptTypePopulatorTest
{
	private ScriptTypePopulator scriptTypePopulator;
	private ScriptRunnerFactory scriptRunnerFactory;
	private DataService dataService;
	private ScriptTypeFactory scriptTypeFactory;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		scriptRunnerFactory = mock(ScriptRunnerFactory.class);
		dataService = mock(DataService.class);
		scriptTypeFactory = mock(ScriptTypeFactory.class);
		scriptTypePopulator = new ScriptTypePopulator(scriptRunnerFactory, dataService, scriptTypeFactory);
	}

	@Test
	public void populate() throws Exception
	{
		String scriptRunner0Name = "scriptRunner0";
		ScriptRunner scriptRunner0 = when(mock(ScriptRunner.class).getName()).thenReturn(scriptRunner0Name).getMock();
		String scriptRunner1Name = "scriptRunner1";
		ScriptRunner scriptRunner1 = when(mock(ScriptRunner.class).getName()).thenReturn(scriptRunner1Name).getMock();
		when(scriptRunnerFactory.getScriptRunners()).thenReturn(newArrayList(scriptRunner0, scriptRunner1));
		ScriptType scriptType0 = mock(ScriptType.class);
		when(dataService.findOneById(SCRIPT_TYPE, scriptRunner0Name, ScriptType.class)).thenReturn(scriptType0);
		when(dataService.findOneById(SCRIPT_TYPE, scriptRunner1Name, ScriptType.class)).thenReturn(null);
		ScriptType scriptType1 = mock(ScriptType.class);
		when(scriptTypeFactory.create(scriptRunner1Name)).thenReturn(scriptType1);
		scriptTypePopulator.populate();
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).add(eq(SCRIPT_TYPE), captor.capture());
		assertEquals(captor.getValue().collect(toList()), singletonList(scriptType1));
	}

	// regression test for https://github.com/molgenis/molgenis/issues/5168
	@SuppressWarnings("unchecked")
	@Test
	public void populateNoChanges() throws Exception
	{
		String scriptRunner0Name = "scriptRunner0";
		ScriptRunner scriptRunner0 = when(mock(ScriptRunner.class).getName()).thenReturn(scriptRunner0Name).getMock();
		String scriptRunner1Name = "scriptRunner1";
		ScriptRunner scriptRunner1 = when(mock(ScriptRunner.class).getName()).thenReturn(scriptRunner1Name).getMock();
		when(scriptRunnerFactory.getScriptRunners()).thenReturn(newArrayList(scriptRunner0, scriptRunner1));
		ScriptType scriptType0 = mock(ScriptType.class);
		when(dataService.findOneById(SCRIPT_TYPE, scriptRunner0Name, ScriptType.class)).thenReturn(scriptType0);
		ScriptType scriptType1 = mock(ScriptType.class);
		when(dataService.findOneById(SCRIPT_TYPE, scriptRunner1Name, ScriptType.class)).thenReturn(scriptType1);
		scriptTypePopulator.populate();
		verify(dataService, times(0)).add(eq(SCRIPT_TYPE), any(Stream.class));
	}
}