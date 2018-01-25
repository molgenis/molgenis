package org.molgenis.security.token;

import java.lang.annotation.*;

/**
 * Annotation that you can use to bind a String parameter in a handler method to the molgenis token in the request.
 * Sample use:
 * <p>
 * <pre>
 * @GetMapping("/blah")
 * public void getBlah(@TokenParam String token){
 * ...
 * }
 * </pre>
 * The token may be provided both as a header and as a request param so this way you can easily pick it up no matter
 * where it was specified and also specify that the token is required.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TokenParam
{
	/**
	 * Whether the parameter is required.
	 * <p>Defaults to {@code false}, leading to a {@code null} value if the parameter is
	 * not present in the request. Switch this to
	 * {@code true} if you prefer an exception being thrown
	 * if the parameter is missing in the request.
	 */
	boolean required() default false;
}