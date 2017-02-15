package uk.gov.dstl.baleen.consumers.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.types.BaleenAnnotation;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractJsonConsumer extends BaleenConsumer {

  public static final String PARAM_OUTPUT_FILE = "outputDirectory";
  @ConfigurationParameter(name = PARAM_OUTPUT_FILE, defaultValue = "jsonOutput")
  private String outputDirectory = "jsonOutput";

  private final ObjectMapper objectMapper;

  public AbstractJsonConsumer() {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
    super.doInitialize(aContext);
    getMonitor()
        .info("Will be writing to " + Paths.get(outputDirectory).toAbsolutePath().toString());
  }

  @Override
  protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
    String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());

    try (Writer writer = createOutputWriter(documentSourceName)) {
      writeJson(writer, selectAnnotations(jCas));
    } catch (IOException e) {
      getMonitor().warn("Failed to write JSON for " + documentSourceName, e);
    }
  }

  private Writer createOutputWriter(final String documentSourceName) throws IOException {
    Path directoryPath = Paths.get(outputDirectory);
    if (!Files.exists(directoryPath)) {
      Files.createDirectories(directoryPath);
    }
    String baseName = FilenameUtils.getBaseName(documentSourceName);
    Path outputFilePath = directoryPath.resolve(baseName + ".properties");
    if (Files.exists(outputFilePath)) {
      getMonitor().warn("Overwriting existing output file {}", outputFilePath.toString());
    }
    return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
  }

  private void writeJson(Writer writer, Iterable<? extends BaleenAnnotation> selectedAnnotations)
      throws IOException {
    List<JsonAnnotation> jsonAnnotations = new ArrayList<>();
    for (BaleenAnnotation annotation : selectedAnnotations) {
      jsonAnnotations.add(new JsonAnnotation(annotation));
    }
    objectMapper.writeValue(writer, jsonAnnotations);
  }

  protected abstract Iterable<? extends BaleenAnnotation> selectAnnotations(JCas jCas);

  static final class JsonAnnotation {
    private int begin;
    private int end;
    private String type;
    private Map<String, String> attributes;

    public JsonAnnotation(BaleenAnnotation annotation) {
      this.begin = annotation.getBegin();
      this.end = annotation.getEnd();
      this.type = annotation.getType().getName();
      this.attributes = makeAttributes(annotation);
    }

    private Map<String, String> makeAttributes(BaleenAnnotation annotation) {
      Map<String, String> attributeValues = new HashMap<>();
      List<Feature> features = annotation.getType().getFeatures();
      for (Feature feature : features) {
        // for now coerce all feature values to strings, ignore non-primitives
        if (feature.getRange().isPrimitive()) {
          attributeValues.put(feature.getName(), annotation.getFeatureValueAsString(feature));
        }
      }
      return attributeValues;
    }

    public int getBegin() {
      return begin;
    }

    public int getEnd() {
      return end;
    }

    public String getType() {
      return type;
    }

    public Map<String, String> getAttributes() {
      return attributes;
    }
  }
}