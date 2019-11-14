package org.molgenis.api.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.api.model.response.LinksResponse;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class LinksUtilsTest extends AbstractMockitoTest {

  private MockHttpServletRequest request;

  @BeforeEach
  void setUpBeforeEach() {
    request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  void createLinksResponse() throws URISyntaxException {
    request.setQueryString("page=1");
    LinksResponse linksResponse =
        LinksResponse.builder()
            .setPrevious(new URI("http://localhost?page=0"))
            .setSelf(new URI("http://localhost?page=1"))
            .setNext(new URI("http://localhost?page=2"))
            .build();
    assertEquals(linksResponse, LinksUtils.createLinksResponse(1, 10, 30));
  }

  @Test
  void createLinksResponseFirstPage() throws URISyntaxException {
    request.setQueryString("page=0");
    LinksResponse linksResponse =
        LinksResponse.builder()
            .setSelf(new URI("http://localhost?page=0"))
            .setNext(new URI("http://localhost?page=1"))
            .build();
    assertEquals(linksResponse, LinksUtils.createLinksResponse(0, 10, 30));
  }

  @Test
  void createLinksResponseOnePage() throws URISyntaxException {
    request.setQueryString("page=0");
    LinksResponse linksResponse =
        LinksResponse.builder().setSelf(new URI("http://localhost?page=0")).build();
    assertEquals(linksResponse, LinksUtils.createLinksResponse(0, 10, 10));
  }

  @Test
  void createLinksResponseLastPage() throws URISyntaxException {
    request.setQueryString("page=2");
    LinksResponse linksResponse =
        LinksResponse.builder()
            .setPrevious(new URI("http://localhost?page=1"))
            .setSelf(new URI("http://localhost?page=2"))
            .build();
    assertEquals(linksResponse, LinksUtils.createLinksResponse(2, 10, 30));
  }
}
