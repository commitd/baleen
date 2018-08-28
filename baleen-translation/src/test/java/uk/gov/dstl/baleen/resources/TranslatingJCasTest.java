// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.resources;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.SofaID;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.Sofa;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.translation.TranslationService;

@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class TranslatingJCasTest {

  private static final String STRING = "Test";

  private static final int INT = 0;

  @Mock private SofaID SOFAID;

  @Mock private Sofa SOFA;

  @Mock private TOP TOP;

  @Mock private TranslationService translationService;

  @Mock private JCas mockCas;

  private TranslatingJCas jCas;

  @Before
  public void setUp() {
    jCas = new TranslatingJCas(mockCas, translationService);
  }

  @Test
  public void testSetDocumentTextTranslates() throws TranslationException {

    String toTranslate = "This is the text to translate";
    String translation = "C'est le texte à traduire";

    when(translationService.translate(toTranslate)).thenReturn(translation);

    jCas.setDocumentText(toTranslate);

    verify(mockCas).setDocumentText(translation);
  }

  @Test
  public void testTextLeftAsOriginalIfException() throws TranslationException {

    String canNotTranslate = "This is the text to translate";
    when(translationService.translate(canNotTranslate)).thenThrow(TranslationException.class);

    jCas.setDocumentText(canNotTranslate);

    verify(mockCas).setDocumentText(canNotTranslate);
  }

  @Test
  public void testRelease() {
    jCas.release();
    verify(mockCas).release();
  }

  @Test
  public void testGetFSIndexRepository() {
    jCas.getFSIndexRepository();
    verify(mockCas).getFSIndexRepository();
  }

  @Test
  public void testGetLowLevelIndexRepository() {
    jCas.getLowLevelIndexRepository();
    verify(mockCas).getLowLevelIndexRepository();
  }

  @Test
  public void testGetCas() {
    jCas.getCas();
    verify(mockCas).getCas();
  }

  @Test
  public void testGetCasImpl() {
    jCas.getCasImpl();
    verify(mockCas).getCasImpl();
  }

  @Test
  public void testGetLowLevelCas() {
    jCas.getLowLevelCas();
    verify(mockCas).getLowLevelCas();
  }

  @Test
  public void testGetTypeInt() {
    jCas.getType(INT);
    verify(mockCas).getType(INT);
  }

  @Test
  public void testGetCasType() {
    jCas.getCasType(INT);
    verify(mockCas).getCasType(INT);
  }

  @Test
  public void testGetTypeTOP() {
    jCas.getType(TOP);
    verify(mockCas).getType(TOP);
  }

  @Test
  public void testGetRequiredType() throws CASException {
    jCas.getRequiredType(STRING);
    verify(mockCas).getRequiredType(STRING);
  }

  @Test
  public void testGetRequiredFeature() throws CASException {
    jCas.getRequiredFeature(null, STRING);
    verify(mockCas).getRequiredFeature(null, STRING);
  }

  @Test
  public void testGetRequiredFeatureDE() {
    jCas.getRequiredFeatureDE(null, STRING, STRING, false);
    verify(mockCas).getRequiredFeatureDE(null, STRING, STRING, false);
  }

  @Test
  public void testPutJfsFromCaddr() {
    jCas.putJfsFromCaddr(INT, TOP);
    verify(mockCas).putJfsFromCaddr(INT, TOP);
  }

  @Test
  public void testGetJfsFromCaddr() {
    jCas.getJfsFromCaddr(INT);
    verify(mockCas).getJfsFromCaddr(INT);
  }

  @Test
  public void testCheckArrayBounds() {
    jCas.checkArrayBounds(INT, INT);
    verify(mockCas).checkArrayBounds(0, INT);
  }

  @Test
  public void testThrowFeatMissing() {
    jCas.throwFeatMissing(STRING, STRING);
    verify(mockCas).throwFeatMissing(STRING, STRING);
  }

  @Test
  public void testGetSofaSofaID() {
    jCas.getSofa(SOFAID);
    verify(mockCas).getSofa(SOFAID);
  }

  @Test
  public void testGetSofa() {
    jCas.getSofa();
    verify(mockCas).getSofa();
  }

  @Test
  public void testCreateView() throws CASException {
    jCas.createView(STRING);
    verify(mockCas).createView(STRING);
  }

  @Test
  public void testGetJCas() throws CASException {
    jCas.getJCas(SOFA);
    verify(mockCas).getJCas(SOFA);
  }

  @Test
  public void testGetJFSIndexRepository() {
    jCas.getJFSIndexRepository();
    verify(mockCas).getJFSIndexRepository();
  }

  @Test
  public void testGetDocumentAnnotationFs() {
    jCas.getDocumentAnnotationFs();
    verify(mockCas).getDocumentAnnotationFs();
  }

  @Test
  public void testGetStringArray0L() {
    jCas.getStringArray0L();
    verify(mockCas).getStringArray0L();
  }

  @Test
  public void testGetIntegerArray0L() {
    jCas.getIntegerArray0L();
    verify(mockCas).getIntegerArray0L();
  }

  @Test
  public void testGetFSArray0L() {
    jCas.getFSArray0L();
    verify(mockCas).getFSArray0L();
  }

  @Test
  public void testProcessInit() {
    jCas.processInit();
    verify(mockCas).processInit();
  }

  @Test
  public void testGetViewString() throws CASException {
    jCas.getView(STRING);
    verify(mockCas).getView(STRING);
  }

  @Test
  public void testGetViewSofaFS() throws CASException {
    jCas.getView(SOFA);
    verify(mockCas).getView(SOFA);
  }

  @Test
  public void testGetTypeSystem() {
    jCas.getTypeSystem();
    verify(mockCas).getTypeSystem();
  }

  @Test
  public void testCreateSofa() {
    jCas.createSofa(SOFAID, STRING);
    verify(mockCas).createSofa(SOFAID, STRING);
  }

  @Test
  public void testGetSofaIterator() {
    jCas.getSofaIterator();
    verify(mockCas).getSofaIterator();
  }

  @Test
  public void testCreateFilteredIterator() {
    jCas.createFilteredIterator(null, null);
    verify(mockCas).createFilteredIterator(null, null);
  }

  @Test
  public void testGetConstraintFactory() {
    jCas.getConstraintFactory();
    verify(mockCas).getConstraintFactory();
  }

  @Test
  public void testCreateFeaturePath() {
    jCas.createFeaturePath();
    verify(mockCas).createFeaturePath();
  }

  @Test
  public void testGetIndexRepository() {
    jCas.getIndexRepository();
    verify(mockCas).getIndexRepository();
  }

  @Test
  public void testFs2listIterator() {
    jCas.fs2listIterator(null);
    verify(mockCas).fs2listIterator(null);
  }

  @Test
  public void testReset() {
    jCas.reset();
    verify(mockCas).reset();
    ;
  }

  @Test
  public void testGetViewName() {
    jCas.getViewName();
    verify(mockCas).getViewName();
  }

  @Test
  public void testSize() {
    jCas.size();
    verify(mockCas).size();
  }

  @Test
  public void testCreateFeatureValuePath() {
    jCas.createFeatureValuePath(STRING);
    verify(mockCas).createFeatureValuePath(STRING);
  }

  @Test
  public void testSetSofaDataString() {
    jCas.setSofaDataString(STRING, STRING);
    verify(mockCas).setSofaDataString(STRING, STRING);
  }

  @Test
  public void testGetDocumentText() {
    jCas.getDocumentText();
    verify(mockCas).getDocumentText();
  }

  @Test
  public void testGetSofaDataString() {
    jCas.getSofaDataString();
    verify(mockCas).getSofaDataString();
  }

  @Test
  public void testSetDocumentLanguage() {
    jCas.setDocumentLanguage(STRING);
    verify(mockCas).setDocumentLanguage(STRING);
  }

  @Test
  public void testGetDocumentLanguage() {
    jCas.getDocumentLanguage();
    verify(mockCas).getDocumentLanguage();
  }

  @Test
  public void testSetSofaDataArray() {
    jCas.setSofaDataArray(SOFA, STRING);
    verify(mockCas).setSofaDataArray(SOFA, STRING);
  }

  @Test
  public void testGetSofaDataArray() {
    jCas.getSofaDataArray();
    verify(mockCas).getSofaDataArray();
  }

  @Test
  public void testSetSofaDataURI() {
    jCas.setSofaDataURI(STRING, STRING);
    verify(mockCas).setSofaDataURI(STRING, STRING);
  }

  @Test
  public void testGetSofaDataURI() {
    jCas.getSofaDataURI();
    verify(mockCas).getSofaDataURI();
  }

  @Test
  public void testGetSofaDataStream() {
    jCas.getSofaDataStream();
    verify(mockCas).getSofaDataStream();
  }

  @Test
  public void testGetSofaMimeType() {
    jCas.getSofaMimeType();
    verify(mockCas).getSofaMimeType();
  }

  @Test
  public void testAddFsToIndexes() {
    jCas.addFsToIndexes(SOFA);
    verify(mockCas).addFsToIndexes(SOFA);
  }

  @Test
  public void testRemoveFsFromIndexes() {
    jCas.removeFsFromIndexes(SOFA);
    verify(mockCas).removeFsFromIndexes(SOFA);
  }

  @Test
  public void testRemoveAllIncludingSubtypes() {
    jCas.removeAllIncludingSubtypes(INT);
    verify(mockCas).removeAllIncludingSubtypes(INT);
  }

  @Test
  public void testRemoveAllExcludingSubtypes() {
    jCas.removeAllExcludingSubtypes(INT);
    verify(mockCas).removeAllExcludingSubtypes(INT);
  }

  @Test
  public void testGetAnnotationIndex() {
    jCas.getAnnotationIndex();
    verify(mockCas).getAnnotationIndex();
  }

  @Test
  public void testGetAnnotationIndexType() {
    jCas.getAnnotationIndex(Annotation.class);
    verify(mockCas).getAnnotationIndex(Annotation.class);
  }

  @Test
  public void testGetAnnotationIndexInt() {
    jCas.getAnnotationIndex(INT);
    verify(mockCas).getAnnotationIndex(INT);
  }

  @Test
  public void testGetAnnotationIndexClassOfT() {
    jCas.getAnnotationIndex(Annotation.class);
    verify(mockCas).getAnnotationIndex(Annotation.class);
  }

  @Test
  public void testGetAllIndexedFS() {
    jCas.getAllIndexedFS(TOP.class);
    verify(mockCas).getAllIndexedFS(TOP.class);
  }

  @Test
  public void testGetViewIterator() throws CASException {
    jCas.getViewIterator();
    verify(mockCas).getViewIterator();
  }

  @Test
  public void testGetViewIteratorString() throws CASException {
    jCas.getViewIterator(STRING);
    verify(mockCas).getViewIterator(STRING);
  }

  @Test
  public void testProtectIndexes() {
    jCas.protectIndexes();
    verify(mockCas).protectIndexes();
  }

  @Test
  public void testProtectIndexesRunnable() {
    Runnable r = () -> {};
    jCas.protectIndexes(r);
    verify(mockCas).protectIndexes(r);
  }

  @Test
  public void testGetIndex() {
    jCas.getIndex(STRING, TOP.class);
    verify(mockCas).getIndex(STRING, TOP.class);
  }
}
