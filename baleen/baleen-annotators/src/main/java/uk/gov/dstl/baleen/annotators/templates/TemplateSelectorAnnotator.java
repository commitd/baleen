package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Annotates template fields found in documents using configurable selector queries.
 * <p>
 * The current syntax follows a very limited subset of CSS selectors. The only operators supported
 * are immediate children (>) and the pseudo-selector "nth-of-type(n)", eg: <code>
 * Document > Paragraph:nth-of-type(3)
 * </code>
 * </p>
 */
public class TemplateSelectorAnnotator extends BaleenAnnotator {

  private static final Logger LOGGER = LoggerFactory.getLogger(TemplateSelectorAnnotator.class);

  private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

  public static final String PARAM_SELECTORS_DIRECTORY = "selectorsDirectory";

  @ConfigurationParameter(name = PARAM_SELECTORS_DIRECTORY, defaultValue = "templateSelectors")
  private String selectorsDirectory = "templateSelectors";

  private Map<String, String> selectors = new HashMap<>();

  @Override
  public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
    super.doInitialize(aContext);
    readSelectors();
  }

  private void readSelectors() throws ResourceInitializationException {
    final Path path = Paths.get(selectorsDirectory);
    try {
      Files.list(path).filter(Files::isRegularFile).forEach(this::readSelectorsFromPath);
    } catch (IOException e) {
      throw new ResourceInitializationException(
          new FileNotFoundException("Template selector path not found: " + path.toAbsolutePath()));
    }
  }

  private void readSelectorsFromPath(final Path path) {
    final Properties selectorsFromPath = new Properties();
    try {
      selectorsFromPath.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
      /*
       * perhaps this should map the keys to include the basename of the path they were read from as
       * an anti-collision and (during downstream annotation processing) disambiguation mechanism?
       */
      selectorsFromPath.stringPropertyNames()
          .forEach(p -> selectors.put(p, selectorsFromPath.getProperty(p)));
    } catch (IOException e) {
      LOGGER.warn("Failed to read from selectors file {}", path);
    }
  }

  @Override
  protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
    for (Entry<String, String> selector : selectors.entrySet()) {
      List<? extends Structure> selected;
      try {
        selected = SelectorUtils.select(jCas, selector.getValue(), DEFAULT_STRUCTURAL_PACKAGE);
        selected.forEach(s -> addAnnotation(jCas, selector.getKey(), s));
      } catch (InvalidParameterException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
  }

  private void addAnnotation(JCas jCas, String key, Structure element) {
    TemplateField templateField = new TemplateField(jCas);
    templateField.setName(key);
    templateField.setBegin(element.getBegin());
    templateField.setEnd(element.getEnd());
    addToJCasIndex(templateField);
  }

}
