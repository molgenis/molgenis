package org.molgenis.security.token;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Gets the molgenis authentication token from a HttpServletRequest
 */
public class TokenExtractor implements HandlerMethodArgumentResolver
{
	public static final String TOKEN_HEADER = "x-molgenis-token";
	public static final String TOKEN_PARAMETER = "molgenis-token";

	public static String getToken(HttpServletRequest request)
	{
		return getTokenInternal(request.getHeader(TOKEN_HEADER), request.getParameter(TOKEN_PARAMETER));
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws ServletRequestBindingException
	{
		return getTokenInternal(webRequest.getHeader(TOKEN_HEADER), webRequest.getParameter(TOKEN_PARAMETER),
				parameter.getParameterAnnotation(TokenParam.class).required());
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return parameter.hasParameterAnnotation(TokenParam.class);
	}

	private static String getTokenInternal(String tokenHeader, String tokenParam, boolean required)
			throws ServletRequestBindingException
	{
		Optional<String> token = Optional.ofNullable(getTokenInternal(tokenHeader, tokenParam));
		if (!token.isPresent() && required)
		{
			throw new ServletRequestBindingException(
					"Missing molgenis token. Token should either be present in the " + TOKEN_HEADER
							+ " request header or the " + TOKEN_PARAMETER + " parameter.");
		}
		return token.orElse(null);
	}

	private static String getTokenInternal(String tokenHeader, String tokenParam)
	{
		return Stream.of(tokenHeader, tokenParam).filter(StringUtils::isNotBlank).findFirst().orElse(null);
	}
}
