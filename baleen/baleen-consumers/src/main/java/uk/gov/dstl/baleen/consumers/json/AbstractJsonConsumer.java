package uk.gov.dstl.baleen.consumers.json;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.Sofa;
import org.apache.uima.resource.ResourceInitializationException;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.uima.BaleenConsumer;

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
		getMonitor().info("Will be writing to " + Paths.get(outputDirectory).toAbsolutePath().toString());
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());

		JsonFactory factory = objectMapper.getFactory();
		try (Writer writer = createOutputWriter(documentSourceName);
				JsonGenerator generator = factory.createGenerator(writer).useDefaultPrettyPrinter()) {
			generator.writeStartObject();
			writeSofa(generator, jCas);
			writeAnnotations(generator, selectAnnotations(jCas));
			generator.writeEndObject();
		} catch (IOException e) {
			getMonitor().warn("Failed to write JSON for " + documentSourceName, e);
		}
	}

	private void writeSofa(JsonGenerator generator, JCas jCas) throws IOException {
		Sofa sofa = jCas.getSofa();
		generator.writeFieldName("sofa");
		writeFS(generator, sofa);
	}

	private Writer createOutputWriter(final String documentSourceName) throws IOException {
		Path directoryPath = Paths.get(outputDirectory);
		if (!Files.exists(directoryPath)) {
			Files.createDirectories(directoryPath);
		}
		String baseName = FilenameUtils.getBaseName(documentSourceName);
		Path outputFilePath = directoryPath.resolve(baseName + ".json");
		if (Files.exists(outputFilePath)) {
			getMonitor().warn("Overwriting existing output file {}", outputFilePath.toString());
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}

	private void writeAnnotations(JsonGenerator generator, Iterable<? extends FeatureStructure> selectedAnnotations)
			throws IOException {
		generator.writeFieldName("annotations");
		generator.writeStartArray();
		for (FeatureStructure baleenAnnotation : selectedAnnotations) {
			writeFS(generator, baleenAnnotation);
		}
		generator.writeEndArray();
	}

	private void writeFS(JsonGenerator generator, FeatureStructure annotation) throws IOException {
		generator.writeStartObject();

		Type type = annotation.getType();
		generator.writeStringField("type", type.getName());

		List<Feature> features = type.getFeatures();
		if (annotation instanceof AnnotationFS) {
			AnnotationFS annotationFS = (AnnotationFS) annotation;
			if (!(annotationFS.getEnd() == 0 && annotationFS.getBegin() == 0)) {
				generator.writeStringField("coveredText", annotationFS.getCoveredText());
			}
		}

		if (features.size() > 0) {
			generator.writeObjectFieldStart("fields");
			for (Feature feature : features) {
				if (feature.getRange().isPrimitive()) {
					writePrimitive(generator, annotation, feature);
				} else if (feature.getRange().isArray()) {
					writeArray(generator, annotation, feature);
				} else {
					FeatureStructure featureValue = annotation.getFeatureValue(feature);
					if (featureValue != null) {
						if ("uima.cas.AnnotationBase:sofa".equals(feature.getName())) {
							continue;
						}
						generator.writeFieldName(feature.getShortName());
						writeFS(generator, featureValue);
					}
				}
			}
			generator.writeEndObject();
		}
		generator.writeEndObject();
	}

	private void writeArray(JsonGenerator generator, FeatureStructure annotation, Feature feature) throws IOException {
		if (annotation instanceof FSArray) {
			FSArray fsArray = (FSArray) annotation;
			generator.writeFieldName(feature.getShortName());
			FeatureStructure[] array = fsArray.toArray();
			if (array.length > 0) {
				generator.writeStartArray();
				for (FeatureStructure featureStructure : array) {
					writePrimitiveValue(generator, featureStructure, feature);
				}
			}
			generator.writeEndArray();
		}
	}

	private void writePrimitive(JsonGenerator generator, FeatureStructure annotation, Feature feature)
			throws IOException {
		generator.writeFieldName(feature.getShortName());
		writePrimitiveValue(generator, annotation, feature);
	}

	private void writePrimitiveValue(JsonGenerator generator, FeatureStructure annotation, Feature feature)
			throws IOException {
		String range = feature.getRange().getName();
		switch (range) {
		case CAS.TYPE_NAME_INTEGER:
			generator.writeNumber(annotation.getIntValue(feature));
			break;
		case CAS.TYPE_NAME_FLOAT:
			generator.writeNumber(annotation.getFloatValue(feature));
			break;
		case CAS.TYPE_NAME_STRING:
			generator.writeString(annotation.getStringValue(feature));
			break;
		case CAS.TYPE_NAME_BOOLEAN:
			generator.writeBoolean(annotation.getBooleanValue(feature));
			break;
		case CAS.TYPE_NAME_BYTE:
			generator.writeNumber(annotation.getByteValue(feature));
			break;
		case CAS.TYPE_NAME_SHORT:
			generator.writeNumber(annotation.getShortValue(feature));
			break;
		case CAS.TYPE_NAME_LONG:
			generator.writeNumber(annotation.getLongValue(feature));
			break;
		case CAS.TYPE_NAME_DOUBLE:
			generator.writeNumber(annotation.getDoubleValue(feature));
			break;
		default:
			getMonitor().warn("Unexpected primitive type: " + range);
			break;
		}
	}

	protected abstract Iterable<? extends FeatureStructure> selectAnnotations(JCas jCas);

}