package org.molgenis.integrationtest.config;

import com.google.gson.Gson;
import org.molgenis.validation.JsonValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({Gson.class, JsonValidator.class})
public class JsonTestConfig {}
