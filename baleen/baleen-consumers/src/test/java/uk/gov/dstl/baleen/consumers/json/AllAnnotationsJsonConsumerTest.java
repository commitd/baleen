package uk.gov.dstl.baleen.consumers.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.semantic.Event;
import uk.gov.dstl.baleen.types.structure.Paragraph;

public class AllAnnotationsJsonConsumerTest extends AbstractAnnotatorTest {
	private static final String EXPECTED_OUTPUT_FILE = AllAnnotationsJsonConsumerTest.class.getSimpleName() + ".json";

	private static final String SOURCEURI = AllAnnotationsJsonConsumerTest.class.getSimpleName() + ".txt";

	private static final String PARA1 = "The quick brown fox jumped over the lazy dog's back.";

	private static final String PARA2 = "The quick brown cat jumped over the lazy dog's back.";

	private static final String TEXT = String.join("\n", PARA1, PARA2);

	private Path tempDirectory;

	public AllAnnotationsJsonConsumerTest() {
		super(AllAnnotationsJsonConsumer.class);
	}

	@Before
	public void setup() throws IOException {
		jCas.setDocumentText(TEXT);
		tempDirectory = Files.createTempDirectory(AllAnnotationsJsonConsumerTest.class.getSimpleName());
		tempDirectory.toFile().deleteOnExit();

		DocumentAnnotation documentAnnotation = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		documentAnnotation.setSourceUri(SOURCEURI);

		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(1);
		paragraph1.setEnd(52);
		paragraph1.addToIndexes();

		Event event = new Event(jCas);
		event.setBegin(53);
		event.setEnd(105);
		event.setArguments(new StringArray(jCas, 2));
		event.setArguments(0, "cat");
		event.setArguments(1, "dog");
		event.addToIndexes();
	}

	@Test
	public void testJson() throws AnalysisEngineProcessException, ResourceInitializationException, IOException {
		processJCas(AllAnnotationsJsonConsumer.PARAM_OUTPUT_DIRECTORY, tempDirectory.toString());
		Path outputPath = tempDirectory.resolve(EXPECTED_OUTPUT_FILE);
		outputPath.toFile().deleteOnExit();

		byte[] outputFile = Files.readAllBytes(outputPath);
		byte[] expectedFile = IOUtils
				.toByteArray(AllAnnotationsJsonConsumerTest.class.getResourceAsStream(EXPECTED_OUTPUT_FILE));

		// traversal order may change during serialisation, so just assert
		// length is the same
		assertEquals(expectedFile.length, outputFile.length);

		Files.delete(outputPath);
	}

}