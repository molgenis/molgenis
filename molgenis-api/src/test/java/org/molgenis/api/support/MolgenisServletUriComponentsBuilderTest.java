package org.molgenis.api.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class MolgenisServletUriComponentsBuilderTest extends AbstractMockitoTest {
  @Mock HttpServletRequest request;

  @BeforeEach
  void setUpBeforeEach() {
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  void testFromCurrentRequestDecodedQueryNoWithoutQuery() {
    when(request.getRequestURI()).thenReturn("/api/data/EntityType");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(80);

    assertEquals(
        "http://localhost/api/data/EntityType",
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery().build().toUriString());
  }

  @Test
  void testFromCurrentRequestDecodedQueryWithQuery() {
    Map<String, String[]> params = new HashMap<>();
    params.put("query", new String[] {"a==b,c=in=('d e','f g','h i')&j==k"});
    params.put("page", new String[] {"1"});
    params.put("attr", new String[] {"#CHROM,POS,REF,ALT"});

    when(request.getRequestURI()).thenReturn("/api/data/EntityType");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(80);
    when(request.getParameterMap()).thenReturn(params);

    assertEquals(
        "http://localhost/api/data/EntityType?query=a==b,c=in=('d e','f g','h i')&j==k&page=1&attr=#CHROM,POS,REF,ALT",
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery().build().toUriString());
  }
}
