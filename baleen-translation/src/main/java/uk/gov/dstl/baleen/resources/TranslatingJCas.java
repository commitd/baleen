// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.resources;

import java.io.InputStream;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.ConstraintFactory;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIndexRepository;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FSMatchConstraint;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeaturePath;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.FeatureValuePath;
import org.apache.uima.cas.SofaFS;
import org.apache.uima.cas.SofaID;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.LowLevelCAS;
import org.apache.uima.cas.impl.LowLevelIndexRepository;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.Sofa;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.translation.TranslationService;

/**
 * A delegating wrapper for a {@link JCas} that translates the set document text using the given
 * {@link TranslationService}.
 */
@SuppressWarnings({"deprecation", "squid:CallToDeprecatedMethod"}) // From delegate
public class TranslatingJCas implements JCas {

  private final JCas delegate;

  private final TranslationService translationService;

  /**
   * Construct the translating JCas to wrap the given delegate and the translation service to use
   * when translating.
   *
   * @param delegate
   * @param translationService
   */
  public TranslatingJCas(JCas delegate, TranslationService translationService) {
    this.delegate = delegate;
    this.translationService = translationService;
  }

  /**
   * Translates the set document text if possible.
   *
   * @see org.apache.uima.jcas.JCas#setDocumentText(java.lang.String)
   */
  @Override
  public void setDocumentText(String text) {
    try {
      String translation = translationService.translate(text);
      delegate.setDocumentText(translation);
    } catch (TranslationException e) {
      delegate.setDocumentText(text);
    }
  }

  // Delegate methods

  @Override
  public void release() {
    delegate.release();
  }

  @Override
  public FSIndexRepository getFSIndexRepository() {
    return delegate.getFSIndexRepository();
  }

  @Override
  public LowLevelIndexRepository getLowLevelIndexRepository() {
    return delegate.getLowLevelIndexRepository();
  }

  @Override
  public CAS getCas() {
    return delegate.getCas();
  }

  @Override
  public CASImpl getCasImpl() {
    return delegate.getCasImpl();
  }

  @Override
  public LowLevelCAS getLowLevelCas() {
    return delegate.getLowLevelCas();
  }

  @Override
  public TOP_Type getType(int i) {
    return delegate.getType(i);
  }

  @Override
  public Type getCasType(int i) {
    return delegate.getCasType(i);
  }

  @Override
  public TOP_Type getType(TOP instance) {
    return delegate.getType(instance);
  }

  @Override
  public Type getRequiredType(String s) throws CASException {
    return delegate.getRequiredType(s);
  }

  @Override
  public Feature getRequiredFeature(Type t, String s) throws CASException {
    return delegate.getRequiredFeature(t, s);
  }

  @Override
  public Feature getRequiredFeatureDE(Type t, String s, String rangeName, boolean featOkTst) {
    return delegate.getRequiredFeatureDE(t, s, rangeName, featOkTst);
  }

  @Override
  public void putJfsFromCaddr(int casAddr, FeatureStructure fs) {
    delegate.putJfsFromCaddr(casAddr, fs);
  }

  @Override
  public <T extends TOP> T getJfsFromCaddr(int casAddr) {
    return delegate.getJfsFromCaddr(casAddr);
  }

  @Override
  public void checkArrayBounds(int fsRef, int pos) {
    delegate.checkArrayBounds(fsRef, pos);
  }

  @Override
  public void throwFeatMissing(String feat, String type) {
    delegate.throwFeatMissing(feat, type);
  }

  @Override
  public Sofa getSofa(SofaID sofaID) {
    return delegate.getSofa(sofaID);
  }

  @Override
  public Sofa getSofa() {
    return delegate.getSofa();
  }

  @Override
  public JCas createView(String sofaID) throws CASException {
    return delegate.createView(sofaID);
  }

  @Override
  public JCas getJCas(Sofa sofa) throws CASException {
    return delegate.getJCas(sofa);
  }

  @Override
  public JFSIndexRepository getJFSIndexRepository() {
    return delegate.getJFSIndexRepository();
  }

  @Override
  public TOP getDocumentAnnotationFs() {
    return delegate.getDocumentAnnotationFs();
  }

  @Override
  public StringArray getStringArray0L() {
    return delegate.getStringArray0L();
  }

  @Override
  public IntegerArray getIntegerArray0L() {
    return delegate.getIntegerArray0L();
  }

  @Override
  public FSArray getFSArray0L() {
    return delegate.getFSArray0L();
  }

  @Override
  public void processInit() {
    delegate.processInit();
  }

  @Override
  public JCas getView(String localViewName) throws CASException {
    return delegate.getView(localViewName);
  }

  @Override
  public JCas getView(SofaFS aSofa) throws CASException {
    return delegate.getView(aSofa);
  }

  @Override
  public TypeSystem getTypeSystem() {
    return delegate.getTypeSystem();
  }

  @Override
  public SofaFS createSofa(SofaID sofaID, String mimeType) {
    return delegate.createSofa(sofaID, mimeType);
  }

  @Override
  public FSIterator<SofaFS> getSofaIterator() {
    return delegate.getSofaIterator();
  }

  @Override
  public <T extends FeatureStructure> FSIterator<T> createFilteredIterator(
      FSIterator<T> it, FSMatchConstraint cons) {
    return delegate.createFilteredIterator(it, cons);
  }

  @Override
  public ConstraintFactory getConstraintFactory() {
    return delegate.getConstraintFactory();
  }

  @Override
  public FeaturePath createFeaturePath() {
    return delegate.createFeaturePath();
  }

  @Override
  public FSIndexRepository getIndexRepository() {
    return delegate.getIndexRepository();
  }

  @Override
  public <T extends FeatureStructure> ListIterator<T> fs2listIterator(FSIterator<T> it) {
    return delegate.fs2listIterator(it);
  }

  @Override
  public void reset() {
    delegate.reset();
  }

  @Override
  public String getViewName() {
    return delegate.getViewName();
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public FeatureValuePath createFeatureValuePath(String featureValuePath) {
    return delegate.createFeatureValuePath(featureValuePath);
  }

  @Override
  public void setSofaDataString(String text, String mimetype) {
    delegate.setSofaDataString(text, mimetype);
  }

  @Override
  public String getDocumentText() {
    return delegate.getDocumentText();
  }

  @Override
  public String getSofaDataString() {
    return delegate.getSofaDataString();
  }

  @Override
  public void setDocumentLanguage(String languageCode) {
    delegate.setDocumentLanguage(languageCode);
  }

  @Override
  public String getDocumentLanguage() {
    return delegate.getDocumentLanguage();
  }

  @Override
  public void setSofaDataArray(FeatureStructure array, String mime) {
    delegate.setSofaDataArray(array, mime);
  }

  @Override
  public FeatureStructure getSofaDataArray() {
    return delegate.getSofaDataArray();
  }

  @Override
  public void setSofaDataURI(String uri, String mime) {
    delegate.setSofaDataURI(uri, mime);
  }

  @Override
  public String getSofaDataURI() {
    return delegate.getSofaDataURI();
  }

  @Override
  public InputStream getSofaDataStream() {
    return delegate.getSofaDataStream();
  }

  @Override
  public String getSofaMimeType() {
    return delegate.getSofaMimeType();
  }

  @Override
  public void addFsToIndexes(FeatureStructure fs) {
    delegate.addFsToIndexes(fs);
  }

  @Override
  public void removeFsFromIndexes(FeatureStructure fs) {
    delegate.removeFsFromIndexes(fs);
  }

  @Override
  public void removeAllIncludingSubtypes(int i) {
    delegate.removeAllIncludingSubtypes(i);
  }

  @Override
  public void removeAllExcludingSubtypes(int i) {
    delegate.removeAllExcludingSubtypes(i);
  }

  @Override
  public AnnotationIndex<Annotation> getAnnotationIndex() {
    return delegate.getAnnotationIndex();
  }

  @Override
  public <T extends Annotation> AnnotationIndex<T> getAnnotationIndex(Type type) {
    return delegate.getAnnotationIndex(type);
  }

  @Override
  public <T extends Annotation> AnnotationIndex<T> getAnnotationIndex(int type) {
    return delegate.getAnnotationIndex(type);
  }

  @Override
  public <T extends Annotation> AnnotationIndex<T> getAnnotationIndex(Class<T> clazz) {
    return delegate.getAnnotationIndex(clazz);
  }

  @Override
  public <T extends TOP> FSIterator<T> getAllIndexedFS(Class<T> clazz) {
    return delegate.getAllIndexedFS(clazz);
  }

  @Override
  public Iterator<JCas> getViewIterator() throws CASException {
    return delegate.getViewIterator();
  }

  @Override
  public Iterator<JCas> getViewIterator(String localViewNamePrefix) throws CASException {
    return delegate.getViewIterator(localViewNamePrefix);
  }

  @Override
  public AutoCloseable protectIndexes() {
    return delegate.protectIndexes();
  }

  @Override
  public void protectIndexes(Runnable runnable) {
    delegate.protectIndexes(runnable);
  }

  @Override
  public <T extends TOP> FSIndex<T> getIndex(String label, Class<T> clazz) {
    return delegate.getIndex(label, clazz);
  }
}
