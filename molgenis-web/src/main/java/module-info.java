module molgenis.web {
	requires slf4j.api;
	requires spring.beans;
	requires spring.core;
	requires spring.web;
	requires spring.context;
	requires spring.webmvc;
	requires javax.servlet.api;
	requires jsr305;
	requires molgenis.i18n;
	requires molgenis.data;
	requires molgenis.data.security;
	requires molgenis.security.core;
	requires molgenis.data.plugin;
	requires guava;
	requires molgenis.settings;
	requires spring.security.web;
	exports org.molgenis.web;
}