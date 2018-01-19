module molgenis.js {
	requires spring.context;
	requires slf4j.api;
	requires guava;
	requires molgenis.data;
	requires molgenis.scripts.core;
	requires molgenis.util;
	requires java.scripting;
	requires jdk.scripting.nashorn;
	exports org.molgenis.js.magma;
}