package uk.gov.dstl.baleen.uima.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.Reflections;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;

import java.util.List;
import java.util.Set;

public class SelectorUtilsTest {

	private static final String TEXT = "The quick brown fox jumped over the lazy dogs back";

	private static final String PACKAGE = "uk.gov.dstl.baleen.types.structure";

	private static Set<Class<? extends Structure>> structuralClasses;

	@BeforeClass
	public static void initClasses() {
		Reflections reflections = new Reflections(PACKAGE);
		structuralClasses = reflections.getSubTypesOf(Structure.class);
	}

	private JCas jCas;

	@Before
	public void setup() throws UIMAException {
		jCas = JCasSingleton.getJCasInstance();
		jCas.setDocumentText(TEXT);
	}

	@Test
	public void testSelectSimple() throws UIMAException, InvalidParameterException {
		Paragraph paragraph = new Paragraph(jCas);
		paragraph.setBegin(0);
		paragraph.setDepth(1);
		paragraph.setEnd(TEXT.length());
		paragraph.addToIndexes();

		List<? extends Structure> select = SelectorUtils.select(jCas, "Paragraph", PACKAGE);
		assertEquals(1, select.size());
		assertEquals(paragraph, select.get(0));
	}

	@Test
	public void testSelectNthTwo() throws InvalidParameterException {
		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(1);
		paragraph1.setEnd(20);
		paragraph1.addToIndexes();

		Paragraph paragraph2 = new Paragraph(jCas);
		paragraph2.setBegin(20);
		paragraph2.setDepth(1);
		paragraph2.setEnd(TEXT.length());
		paragraph2.addToIndexes();

		List<? extends Structure> select1 = SelectorUtils.select(jCas, "Paragraph:nth-of-type(1)", PACKAGE);
		assertEquals(1, select1.size());
		assertEquals(paragraph1, select1.get(0));
		assertNotEquals(paragraph2, select1.get(0));

		List<? extends Structure> select2 = SelectorUtils.select(jCas, "Paragraph:nth-of-type(2)", PACKAGE);
		assertEquals(1, select2.size());
		assertEquals(paragraph2, select2.get(0));
		assertNotEquals(paragraph1, select2.get(0));

		List<? extends Structure> select3 = SelectorUtils.select(jCas, "Paragraph:nth-of-type(3)", PACKAGE);
		assertEquals(0, select3.size());

	}

	@Test
	public void testSelectNested() throws InvalidParameterException {
		Section section = new Section(jCas);
		section.setBegin(0);
		section.setDepth(1);
		section.setEnd(TEXT.length());
		section.addToIndexes();

		Paragraph paragraph = new Paragraph(jCas);
		paragraph.setBegin(0);
		paragraph.setDepth(2);
		paragraph.setEnd(TEXT.length());
		paragraph.addToIndexes();

		List<? extends Structure> selectNest1 = SelectorUtils.select(jCas, "Section > Paragraph:nth-of-type(1)",
				PACKAGE);
		assertEquals(1, selectNest1.size());
		assertEquals(paragraph, selectNest1.get(0));

		List<? extends Structure> selectNest2 = SelectorUtils.select(jCas, "Section > Paragraph", PACKAGE);
		assertEquals(1, selectNest2.size());
		assertEquals(paragraph, selectNest2.get(0));

		List<? extends Structure> selectNest3 = SelectorUtils.select(jCas, "Section:nth-of-type(1) > Paragraph",
				PACKAGE);
		assertEquals(1, selectNest3.size());
		assertEquals(paragraph, selectNest3.get(0));

		List<? extends Structure> selectRoot = SelectorUtils.select(jCas, "Section", PACKAGE);
		assertEquals(1, selectRoot.size());
		assertEquals(section, selectRoot.get(0));
	}

	@Test
	public void testGenerateSimple() {
		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(1);
		paragraph1.setEnd(20);
		paragraph1.addToIndexes();

		String generatePath = SelectorUtils.generatePath(jCas, paragraph1, structuralClasses);
		assertEquals("Paragraph", generatePath);
	}

	@Test
	public void testGenerateTwo() {
		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(1);
		paragraph1.setEnd(20);
		paragraph1.addToIndexes();

		Paragraph paragraph2 = new Paragraph(jCas);
		paragraph2.setBegin(20);
		paragraph2.setDepth(1);
		paragraph2.setEnd(TEXT.length());
		paragraph2.addToIndexes();

		String generatePath1 = SelectorUtils.generatePath(jCas, paragraph1, structuralClasses);
		assertEquals("Paragraph:nth-of-type(1)", generatePath1);

		String generatePath2 = SelectorUtils.generatePath(jCas, paragraph2, structuralClasses);
		assertEquals("Paragraph:nth-of-type(2)", generatePath2);
	}

	@Test
	public void testGenerateNested() {
		Section section = new Section(jCas);
		section.setBegin(0);
		section.setDepth(1);
		section.setEnd(TEXT.length());
		section.addToIndexes();

		Paragraph paragraph = new Paragraph(jCas);
		paragraph.setBegin(0);
		paragraph.setDepth(2);
		paragraph.setEnd(TEXT.length());
		paragraph.addToIndexes();

		String generatePath = SelectorUtils.generatePath(jCas, paragraph, structuralClasses);
		assertEquals("Section > Paragraph", generatePath);
	}

	@Test
	public void testGenerateNested2() {
		Section section = new Section(jCas);
		section.setBegin(0);
		section.setDepth(1);
		section.setEnd(TEXT.length());
		section.addToIndexes();

		Paragraph paragraph1 = new Paragraph(jCas);
		paragraph1.setBegin(0);
		paragraph1.setDepth(2);
		paragraph1.setEnd(20);
		paragraph1.addToIndexes();

		Paragraph paragraph2 = new Paragraph(jCas);
		paragraph2.setBegin(20);
		paragraph2.setDepth(2);
		paragraph2.setEnd(TEXT.length());
		paragraph2.addToIndexes();

		String generatePath1 = SelectorUtils.generatePath(jCas, paragraph1, structuralClasses);
		assertEquals("Section > Paragraph:nth-of-type(1)", generatePath1);

		String generatePath2 = SelectorUtils.generatePath(jCas, paragraph2, structuralClasses);
		assertEquals("Section > Paragraph:nth-of-type(2)", generatePath2);
	}

}
