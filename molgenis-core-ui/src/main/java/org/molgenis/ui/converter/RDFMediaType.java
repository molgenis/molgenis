package org.molgenis.ui.converter;

import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RDFMediaType
{
	public static final MediaType TEXT_TURTLE = new MediaType("text","turtle");
	public static final MediaType APPLICATION_RDF_XML = new MediaType("application","rdf+xml");
	public static final MediaType APPLICATION_X_TURTLE = new MediaType("application","x-turtle");
	public static final MediaType TEXT_RDF_N3 = new MediaType("text","rdf+n3");
	public static final MediaType APPLICATION_JSON = new MediaType("application","json");
	public static final MediaType APPLICATION_TRIG = new MediaType("application", "trig");
	public final static String APPLICATION_TRIG_VALUE = "application/trig";
	public final static String TEXT_TURTLE_VALUE = "text/turtle";


	public static final MediaType APPLICATION_JSONLD = new MediaType("application", "ld+json");
	public static final MediaType APPLICATION_NQUADS = new MediaType("application", "n-quads");


	public static final Set<MediaType> rdfMediaTypes;
	static {

		Set<MediaType> types = new HashSet<>(
				Arrays.asList(new MediaType[]{
						TEXT_TURTLE,
						APPLICATION_RDF_XML,
						APPLICATION_X_TURTLE,
						TEXT_RDF_N3,
						APPLICATION_JSON,
						APPLICATION_TRIG,
						APPLICATION_JSONLD,
						APPLICATION_NQUADS
				})
		);
		rdfMediaTypes = Collections.unmodifiableSet(types);

	}

	public static boolean isRDFMediaType(MediaType mediaType) {
		return rdfMediaTypes.contains(mediaType);
	}
}