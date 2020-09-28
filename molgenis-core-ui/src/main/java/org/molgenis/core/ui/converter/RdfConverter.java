package org.molgenis.core.ui.converter;

import static org.eclipse.rdf4j.rio.RDFFormat.TURTLE;
import static org.eclipse.rdf4j.rio.helpers.BasicWriterSettings.INLINE_BLANK_NODES;
import static org.molgenis.core.ui.converter.RDFMediaType.APPLICATION_TRIG;
import static org.molgenis.core.ui.converter.RDFMediaType.TEXT_TURTLE;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

@Component
public class RdfConverter extends AbstractHttpMessageConverter<Model> {
  private WriterConfig writerConfig;

  public RdfConverter() {
    super(TEXT_TURTLE, APPLICATION_TRIG);
    writerConfig = new WriterConfig().useDefaults();
    writerConfig.set(INLINE_BLANK_NODES, true);
  }

  @Override
  protected boolean supports(Class<?> aClass) {
    return Model.class.isAssignableFrom(aClass);
  }

  @Override
  protected Model readInternal(Class<? extends Model> aClass, HttpInputMessage httpInputMessage) {
    throw new HttpMessageNotReadableException("RDF support is readonly!");
  }

  @Override
  protected void writeInternal(Model model, HttpOutputMessage httpOutputMessage) {
    runAsSystem(
        () -> {
          try {
            Rio.write(model, httpOutputMessage.getBody(), TURTLE, writerConfig);
            httpOutputMessage.getBody().close();
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }
}
