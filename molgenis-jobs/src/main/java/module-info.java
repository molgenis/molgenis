module molgenis.jobs {
	requires molgenis.data;
	requires spring.context;
	requires molgenis.data.security;
	requires commons.lang3;
	requires jsr305;
	requires molgenis.util;
	requires guava;
	requires quartz;
	requires slf4j.api;
	requires gson;
	requires molgenis.security.core;
	requires spring.beans;
	requires spring.context.support;
	requires spring.security.core;
	requires molgenis.data.validation;
	requires spring.tx;
	requires spring.core;
	exports org.molgenis.jobs;
}