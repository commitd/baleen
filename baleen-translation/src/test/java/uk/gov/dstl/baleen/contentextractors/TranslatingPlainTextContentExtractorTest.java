// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.uima.BaleenContentExtractor;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;

@RunWith(MockitoJUnitRunner.class)
public class TranslatingPlainTextContentExtractorTest {

  @Mock private SharedTranslationResource translationService;

  @Test
  public void testTranslates() throws Exception {

    String toTranslate = "This is the text to translate";
    String translation = "C'est le texte à traduire";

    when(translationService.translate(toTranslate)).thenReturn(translation);

    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingPlainTextContentExtractor(translationService);

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    try (InputStream is = new ByteArrayInputStream(toTranslate.getBytes())) {
      contentExtractor.processStream(is, "source", jCas);

      assertEquals(translation, jCas.getDocumentText());

      jCas.reset();
    }

    contentExtractor.destroy();
  }

  @Test
  public void testTextLeftAsOriginalIfException() throws Exception {

    String toTranslate = "This is the text to translate";

    when(translationService.translate(toTranslate)).thenThrow(TranslationException.class);

    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingPlainTextContentExtractor(translationService);

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    try (InputStream is = new ByteArrayInputStream(toTranslate.getBytes())) {
      contentExtractor.processStream(is, "source", jCas);

      assertEquals(toTranslate, jCas.getDocumentText());

      jCas.reset();
    }

    contentExtractor.destroy();
  }
}
