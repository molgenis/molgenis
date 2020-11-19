package org.molgenis.integrationtest.config;

import org.molgenis.js.graal.GraalScriptEngine;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.js.magma.WithJsMagmaScriptAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({JsMagmaScriptEvaluator.class, GraalScriptEngine.class, WithJsMagmaScriptAspect.class})
public class JsTestConfig {}
