package org.molgenis.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gson.Gson;

/**
 * Annotation to allow {@link Gson} to construct @AutoValue annotated classes
 * 
 * @author JakeWharton
 * @see https://gist.github.com/JakeWharton/0d67d01badcee0ae7bc9
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoGson
{
	// A reference to the AutoValue-generated class (e.g. AutoValue_MyClass). This is
	// necessary to handle obfuscation of the class names.
	public Class autoValueClass();
}
