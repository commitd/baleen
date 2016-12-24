// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.collectionreaders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.junit.Test;

import uk.gov.dstl.baleen.collectionreaders.testing.AbstractReaderTest;
import uk.gov.dstl.baleen.types.metadata.Metadata;
import uk.gov.dstl.baleen.uima.BaleenCollectionReader;

<<<<<<< 5d3cd7294db447bf550c0d4d2043a58bf7bc2b9f
public class LineReaderTest extends AbstractReaderTest{
	
	public LineReaderTest(){
		super(LineReader.class);
	}
	
	@Test
	public void test() throws Exception{
		File f = new File(getClass().getResource("lineReader.txt").getPath());
		BaleenCollectionReader bcr = getCollectionReader(LineReader.PARAM_FILE, f.getPath(), LineReader.PARAM_CONTENT_EXTRACTOR, "UimaContentExtractor");
		
		assertTrue(bcr.doHasNext());
		bcr.getNext(jCas.getCas());
		assertEquals("This is the first line", jCas.getDocumentText());
		assertEquals(1, JCasUtil.select(jCas, Metadata.class).size());
		Metadata md = JCasUtil.selectByIndex(jCas, Metadata.class, 0);
		assertEquals("lineNumber", md.getKey());
		assertEquals("1", md.getValue());
		assertTrue(getSource(jCas).endsWith("#1"));
		jCas.reset();
		
		assertTrue(bcr.doHasNext());
		bcr.getNext(jCas.getCas());
		assertEquals("This is the second line", jCas.getDocumentText());
		assertEquals(1, JCasUtil.select(jCas, Metadata.class).size());
		md = JCasUtil.selectByIndex(jCas, Metadata.class, 0);
		assertEquals("lineNumber", md.getKey());
		assertEquals("2", md.getValue());
		assertTrue(getSource(jCas).endsWith("#2"));
		jCas.reset();
		
		assertTrue(bcr.doHasNext());
		bcr.getNext(jCas.getCas());
		assertEquals("This is the fourth line, but the third one we pick out", jCas.getDocumentText());
		md = JCasUtil.selectByIndex(jCas, Metadata.class, 0);
		assertEquals("lineNumber", md.getKey());
		assertEquals("4", md.getValue());
		assertTrue(getSource(jCas).endsWith("#4"));
		jCas.reset();
		
		assertTrue(bcr.doHasNext());
		bcr.getNext(jCas.getCas());
		assertEquals("This is the sixth line, but the fourth and final one we pick out", jCas.getDocumentText());
		md = JCasUtil.selectByIndex(jCas, Metadata.class, 0);
		assertEquals("lineNumber", md.getKey());
		assertEquals("6", md.getValue());
		assertTrue(getSource(jCas).endsWith("#6"));
		jCas.reset();
		
		assertFalse(bcr.doHasNext());
		assertFalse(bcr.doHasNext());
		
		bcr.close();
	}
	
	private String getSource(JCas jCas){
		DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
		return doc.getSourceUri();
	}
=======
public class LineReaderTest {
  JCas jCas;

  @Before
  public void beforeTest() throws Exception {
    if (jCas == null) {
      jCas = JCasFactory.createJCas();
    } else {
      jCas.reset();
    }
  }

  @Test
  public void test() throws Exception {
    final File f = new File(getClass().getResource("lineReader.txt").getPath());
    final BaleenCollectionReader bcr =
        (BaleenCollectionReader) CollectionReaderFactory.createReader(LineReader.class,
            LineReader.PARAM_FILE, f.getPath(), LineReader.PARAM_CONTENT_EXTRACTOR,
            "UimaContentExtractor");

    assertTrue(bcr.doHasNext());
    bcr.getNext(jCas.getCas());
    assertEquals("This is the first line", jCas.getDocumentText());
    assertEquals(2, JCasUtil.select(jCas, Metadata.class).size());
    Metadata md = JCasUtil.selectByIndex(jCas, Metadata.class, 1);
    assertEquals("lineNumber", md.getKey());
    assertEquals("1", md.getValue());
    assertTrue(getSource(jCas).endsWith("#1"));
    jCas.reset();

    assertTrue(bcr.doHasNext());
    bcr.getNext(jCas.getCas());
    assertEquals("This is the second line", jCas.getDocumentText());
    assertEquals(2, JCasUtil.select(jCas, Metadata.class).size());
    md = JCasUtil.selectByIndex(jCas, Metadata.class, 1);
    assertEquals("lineNumber", md.getKey());
    assertEquals("2", md.getValue());
    assertTrue(getSource(jCas).endsWith("#2"));
    jCas.reset();

    assertTrue(bcr.doHasNext());
    bcr.getNext(jCas.getCas());
    assertEquals("This is the fourth line, but the third one we pick out", jCas.getDocumentText());
    md = JCasUtil.selectByIndex(jCas, Metadata.class, 1);
    assertEquals("lineNumber", md.getKey());
    assertEquals("4", md.getValue());
    assertTrue(getSource(jCas).endsWith("#4"));
    jCas.reset();

    assertTrue(bcr.doHasNext());
    bcr.getNext(jCas.getCas());
    assertEquals("This is the sixth line, but the fourth and final one we pick out",
        jCas.getDocumentText());
    md = JCasUtil.selectByIndex(jCas, Metadata.class, 1);
    assertEquals("lineNumber", md.getKey());
    assertEquals("6", md.getValue());
    assertTrue(getSource(jCas).endsWith("#6"));
    jCas.reset();

    assertFalse(bcr.doHasNext());
    assertFalse(bcr.doHasNext());

    bcr.close();
  }

  private String getSource(final JCas jCas) {
    final DocumentAnnotation doc = (DocumentAnnotation) jCas.getDocumentAnnotationFs();
    return doc.getSourceUri();
  }
>>>>>>> Code coverage for baleen-collectionreaders structural elements in excessive of 80% per class
}
