package org.molgenis.integrationtest.config;

import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.js.nashorn.NashornScriptEngine;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ JsMagmaScriptEvaluator.class, NashornScriptEngine.class })
public class JsTestConfig
{
}
