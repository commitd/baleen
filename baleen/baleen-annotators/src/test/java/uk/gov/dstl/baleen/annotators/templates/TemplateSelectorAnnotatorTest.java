package uk.gov.dstl.baleen.annotators.templates;

import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.templates.TemplateField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class TemplateSelectorAnnotatorTest extends AbstractAnnotatorTest {

	private static final String TEXT = "The quick brown fox jumped over the lazy dog's back. The quick brown cat jumped over the lazy dog's back.";

	private Path tempDirectory;

	private Path selectorFile;

	public TemplateSelectorAnnotatorTest() {
		super(TemplateSelectorAnnotator.class);
	}

	@Before
	public void setup() throws IOException {
		tempDirectory = Files.createTempDirectory(TemplateSelectorAnnotatorTest.class.getSimpleName());
		selectorFile = Files.createTempFile(tempDirectory, TemplateSelectorAnnotatorTest.class.getSimpleName(),
				".properties");
		Properties properties = new Properties();
		properties.put("First", "Section > Paragraph:nth-of-type(1)");
		properties.put("Second", "Section > Paragraph:nth-of-type(2)");
		properties.store(Files.newOutputStream(selectorFile), TemplateSelectorAnnotatorTest.class.getName());
	}

	@Test
	public void testCreateFieldAnnotationsFromSelectorFile()
			throws AnalysisEngineProcessException, ResourceInitializationException {

		jCas.setDocumentText(TEXT);
		Section section = new Section(jCas);
		section.setBegin(0);
		section.setDepth(1);
		section.setEnd(TEXT.length());
		section.addToIndexes();

		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(2);
		paragraph1.setEnd(52);
		paragraph1.addToIndexes();

		Paragraph paragraph2 = new Paragraph(jCas);
		paragraph2.setBegin(53);
		paragraph2.setDepth(2);
		paragraph2.setEnd(105);
		paragraph2.addToIndexes();

		processJCas(TemplateSelectorAnnotator.PARAM_SELECTORS_DIRECTORY, tempDirectory.toString());

		TemplateField field1 = JCasUtil.selectByIndex(jCas, TemplateField.class, 0);
		assertEquals(paragraph1.getBegin(), field1.getBegin());
		assertEquals(paragraph1.getEnd(), field1.getEnd());
		assertEquals(paragraph1.getCoveredText(), field1.getCoveredText());

		TemplateField field2 = JCasUtil.selectByIndex(jCas, TemplateField.class, 1);
		assertEquals(paragraph2.getBegin(), field2.getBegin());
		assertEquals(paragraph2.getEnd(), field2.getEnd());
		assertEquals(paragraph2.getCoveredText(), field2.getCoveredText());
	}

	@After
	public void tearDown() throws IOException {
		Files.delete(selectorFile);
		Files.delete(tempDirectory);
	}

}
