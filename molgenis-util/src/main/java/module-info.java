module molgenis.util {
	requires slf4j.api;
	requires guava;
	requires mapdb;
	requires findbugs.annotations;
	requires spring.context;
	requires spring.context.support;
	requires javax.mail;
	requires spring.core;
	
	exports org.molgenis.util;
	exports org.molgenis.util.mail;
	exports org.molgenis.util.stream;
}