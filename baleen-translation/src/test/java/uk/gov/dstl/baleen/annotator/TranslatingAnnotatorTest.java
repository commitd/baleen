// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.annotator;

import static org.junit.Assert.assertEquals;

import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TestConfiguredTranslateService;
import uk.gov.dstl.baleen.types.language.Text;
import uk.gov.dstl.baleen.types.language.Translation;
import uk.gov.dstl.baleen.uima.UimaSupport;

public class TranslatingAnnotatorTest extends AbstractAnnotatorTest {

  public TranslatingAnnotatorTest() {
    super(TranslatingAnnotator.class);
  }

  public ExternalResourceDescription erd(String translation) {
    return ExternalResourceFactory.createExternalResourceDescription(
        SharedTranslationResource.RESOURCE_KEY,
        SharedTranslationResource.class,
        SharedTranslationResource.PARAM_SERVICE,
        TestConfiguredTranslateService.class.getSimpleName(),
        SharedTranslationResource.PARAM_CONFIG,
        translation);
  }

  @Test
  public void testTranslatesWholeDocument() throws Exception {
    String translation = "translation";

    String text = "Hello world, this is a test";
    jCas.setDocumentText(text);

    processJCas(SharedTranslationResource.RESOURCE_KEY, erd(translation));

    assertEquals(1, JCasUtil.select(jCas, Translation.class).size());
    Translation t = JCasUtil.selectByIndex(jCas, Translation.class, 0);
    assertEquals(TestConfiguredTranslateService.TARGET_LANGUAGE, t.getLanguage());
    assertEquals(text, t.getCoveredText());
    assertEquals(translation, t.getTranslation());
  }

  @Test
  public void testTranslatesTextBlocks() throws Exception {

    String translation = "Bonjour";

    String text = "Hello world\nThis is a test";
    jCas.setDocumentText(text);
    Text text1 = new Text(jCas);
    text1.setBegin(0);
    text1.setEnd(11);
    text1.addToIndexes();
    Text text2 = new Text(jCas);
    text2.setBegin(12);
    text2.setEnd(26);
    text2.addToIndexes();

    processJCas(SharedTranslationResource.RESOURCE_KEY, erd(translation));

    assertEquals(2, JCasUtil.select(jCas, Translation.class).size());

    Translation t1 = JCasUtil.selectByIndex(jCas, Translation.class, 0);
    assertEquals(TestConfiguredTranslateService.TARGET_LANGUAGE, t1.getLanguage());
    assertEquals("Hello world", t1.getCoveredText());
    assertEquals(translation, t1.getTranslation());

    Translation t2 = JCasUtil.selectByIndex(jCas, Translation.class, 1);
    assertEquals(TestConfiguredTranslateService.TARGET_LANGUAGE, t2.getLanguage());
    assertEquals("This is a test", t2.getCoveredText());
    assertEquals(translation, t2.getTranslation());
  }

  @Test
  public void testTranslatedWithNullLanguage() throws Exception {

    String translation = "null";

    String text = "Hello world, this is a test";
    jCas.setDocumentText(text);

    processJCas(
        SharedTranslationResource.RESOURCE_KEY,
        erd(translation),
        TranslatingAnnotator.PARAM_CHECK_SOURCE_LANGUAGE,
        true);

    assertEquals(1, JCasUtil.select(jCas, Translation.class).size());
    Translation t = JCasUtil.selectByIndex(jCas, Translation.class, 0);
    assertEquals(TestConfiguredTranslateService.TARGET_LANGUAGE, t.getLanguage());
    assertEquals(text, t.getCoveredText());
    assertEquals(translation, t.getTranslation());
  }

  @Test
  public void testTranslatedWithCorrectLanguage() throws Exception {

    String translation = "correct";

    String text = "Hello world, this is a test";
    jCas.setDocumentText(text);
    DocumentAnnotation da = UimaSupport.getDocumentAnnotation(jCas);
    da.setLanguage(TestConfiguredTranslateService.SOURCE_LANGUAGE);

    processJCas(
        SharedTranslationResource.RESOURCE_KEY,
        erd(translation),
        TranslatingAnnotator.PARAM_CHECK_SOURCE_LANGUAGE,
        true);

    assertEquals(1, JCasUtil.select(jCas, Translation.class).size());
    Translation t = JCasUtil.selectByIndex(jCas, Translation.class, 0);
    assertEquals(TestConfiguredTranslateService.TARGET_LANGUAGE, t.getLanguage());
    assertEquals(text, t.getCoveredText());
    assertEquals(translation, t.getTranslation());
  }

  @Test
  public void testNotTranslatedWithIncorrectLanguage() throws Exception {

    String translation = "incorrect";

    String text = "Hello world, this is a test";
    jCas.setDocumentText(text);
    DocumentAnnotation da = UimaSupport.getDocumentAnnotation(jCas);
    da.setLanguage("wrong");

    processJCas(
        SharedTranslationResource.RESOURCE_KEY,
        erd(translation),
        TranslatingAnnotator.PARAM_CHECK_SOURCE_LANGUAGE,
        true);

    assertEquals(0, JCasUtil.select(jCas, Translation.class).size());
  }
}
