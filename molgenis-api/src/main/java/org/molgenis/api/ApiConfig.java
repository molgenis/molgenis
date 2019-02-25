package org.molgenis.api;

import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableHypermediaSupport(type = HypermediaType.HAL)
public class ApiConfig implements WebMvcConfigurer {}
