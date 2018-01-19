module molgenis.core {
	requires gson;
	requires commons.io;
	requires slf4j.api;
	requires spring.web;
	requires javax.servlet.api;
	requires spring.core;
	requires java.xml;
	requires molgenis.util;
	requires guava;
	exports org.molgenis.core.framework.ui to molgenis.security;
	exports org.molgenis.core.util to molgenis.security;
}