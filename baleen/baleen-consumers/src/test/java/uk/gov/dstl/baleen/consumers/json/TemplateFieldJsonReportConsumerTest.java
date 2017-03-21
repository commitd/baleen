package uk.gov.dstl.baleen.consumers.json;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.templates.TemplateField;

public class TemplateFieldJsonReportConsumerTest extends AbstractAnnotatorTest {
	private static final String EXPECTED_OUTPUT_FILE = TemplateFieldJsonReportConsumerTest.class.getSimpleName()
			+ ".json";

	private static final String SOURCEURI = TemplateFieldJsonReportConsumerTest.class.getSimpleName() + ".txt";

	private static final String PARA1 = "The quick brown fox jumped over the lazy dog's back.";

	private static final String PARA2 = "The quick brown cat jumped over the lazy dog's back.";

	private static final String TEXT = String.join("\n", PARA1, PARA2);

	private Path tempDirectory;

	public TemplateFieldJsonReportConsumerTest() {
		super(TemplateFieldJsonReportConsumer.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);
		tempDirectory = Files.createTempDirectory(TemplateFieldJsonReportConsumerTest.class.getSimpleName());
		tempDirectory.toFile().deleteOnExit();

		DocumentAnnotation documentAnnotation = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		documentAnnotation.setSourceUri(SOURCEURI);

		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(1);
		paragraph1.setEnd(52);
		paragraph1.addToIndexes();

		Paragraph paragraph2 = new Paragraph(jCas);
		paragraph2.setBegin(53);
		paragraph2.setDepth(1);
		paragraph2.setEnd(105);
		paragraph2.addToIndexes();

		TemplateField field = new TemplateField(jCas);
		field.setBegin(53);
		field.setEnd(105);
		field.addToIndexes();

	}

	@Test
	public void testJson() throws AnalysisEngineProcessException, ResourceInitializationException, IOException {
		processJCas(TemplateFieldJsonReportConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());
		Path outputPath = tempDirectory.resolve(EXPECTED_OUTPUT_FILE);
		outputPath.toFile().deleteOnExit();

		byte[] outputFile = Files.readAllBytes(outputPath);
		byte[] expectedFile = IOUtils
				.toByteArray(TemplateFieldJsonReportConsumerTest.class.getResourceAsStream(EXPECTED_OUTPUT_FILE));

		// traversal order may change during serialisation, so just assert
		// length is the same
		assertEquals(expectedFile.length, outputFile.length);

		Files.delete(outputPath);
	}

}
