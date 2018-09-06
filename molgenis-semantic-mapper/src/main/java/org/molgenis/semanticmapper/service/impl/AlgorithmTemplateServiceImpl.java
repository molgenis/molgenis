package org.molgenis.semanticmapper.service.impl;

import static java.util.Objects.requireNonNull;
import static org.molgenis.script.core.ScriptMetaData.SCRIPT;
import static org.molgenis.script.core.ScriptMetaData.TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.js.magma.JsMagmaScriptRunner;
import org.molgenis.script.core.Script;
import org.molgenis.script.core.ScriptParameter;
import org.molgenis.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.semanticsearch.semantic.Hit;
import org.molgenis.semanticsearch.semantic.Hits;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmTemplateServiceImpl implements AlgorithmTemplateService {
  private final DataService dataService;

  public AlgorithmTemplateServiceImpl(DataService dataService) {
    this.dataService = requireNonNull(dataService);
  }

  @Override
  public Stream<AlgorithmTemplate> find(Hits<ExplainedAttribute> attrMatches) {
    // get all algorithm templates
    Stream<Script> jsScripts =
        dataService.findAll(
            SCRIPT, new QueryImpl<Script>().eq(TYPE, JsMagmaScriptRunner.NAME), Script.class);

    // select all algorithm templates that can be used with target and sources
    return jsScripts.flatMap(script -> toAlgorithmTemplate(script, attrMatches));
  }

  private Stream<AlgorithmTemplate> toAlgorithmTemplate(
      Script script, Hits<ExplainedAttribute> attrMatches) {
    // find attribute for each parameter
    boolean paramMatch = true;
    Map<String, String> model = new HashMap<>();
    for (ScriptParameter param : script.getParameters()) {
      Attribute attr = mapParamToAttribute(param, attrMatches);
      if (attr != null) {
        model.put(param.getName(), attr.getName());
      } else {
        paramMatch = false;
        break;
      }
    }

    // create algorithm template if an attribute was found for all parameters
    AlgorithmTemplate algorithmTemplate = new AlgorithmTemplate(script, model);

    return paramMatch ? Stream.of(algorithmTemplate) : Stream.empty();
  }

  private Attribute mapParamToAttribute(
      ScriptParameter param, Hits<ExplainedAttribute> attrMatches) {

    return attrMatches
        .getHits()
        .stream()
        .map(Hit::getResult)
        .filter(entry -> !entry.getExplainedQueryStrings().isEmpty())
        .filter(
            entry ->
                StreamSupport.stream(entry.getExplainedQueryStrings().spliterator(), false)
                    .allMatch(explain -> explain.getTagName().equalsIgnoreCase(param.getName())))
        .map(ExplainedAttribute::getAttribute)
        .findFirst()
        .orElse(null);
  }
}
