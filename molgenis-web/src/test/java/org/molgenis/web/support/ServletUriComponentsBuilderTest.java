package org.molgenis.web.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class ServletUriComponentsBuilderTest {

  @Test
  void testNoQuery() throws URISyntaxException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    when(request.getRequestURI()).thenReturn("/api/data/EntityType");
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(80);

    assertEquals(
        "http://localhost/api/data/EntityType",
        MolgenisServletUriComponentsBuilder.fromCurrentRequestDecodedQuery().build().toUriString());
  }

  @Test
  void testQuery() throws URISyntaxException {
    Map<String, String[]> params = new HashMap<>();
    params.put("query", new String[] {"a==b,c=in=('d e','f g','h i')&j==k"});
    params.put("page", new String[] {"1"});
    params.put("attr", new String[] {"#CHROM,POS,REF,ALT"});

    HttpServletRequest request = mock(HttpServletRequest.class);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
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
