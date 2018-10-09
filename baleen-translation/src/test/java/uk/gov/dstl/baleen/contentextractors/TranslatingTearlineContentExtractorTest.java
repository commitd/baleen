// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
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
public class TranslatingTearlineContentExtractorTest {

  @Mock private SharedTranslationResource translationService;

  @Test
  public void testTranslates() throws Exception {

    String toTranslate = "This is the text to translate";
    String translation = "C'est le texte à traduire";

    when(translationService.translate(toTranslate)).thenReturn(translation);

    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingTearlineContentExtractor(translationService);

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
        new TranslatingTearlineContentExtractor(translationService);

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    try (InputStream is = new ByteArrayInputStream(toTranslate.getBytes())) {
      contentExtractor.processStream(is, "source", jCas);

      assertEquals(toTranslate, jCas.getDocumentText());

      jCas.reset();
    }

    contentExtractor.destroy();
  }

  @Test
  public void testTearlineTranslated() throws Exception {

    String toTranslate = "This is the first tearline.";
    String translation = "Ceci est la première ligne de larme.";

    when(translationService.translate(eq(toTranslate))).thenReturn(translation);

    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingTearlineContentExtractor(translationService);

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    String[] files = new String[] {"1.docx", "2.docx", "3.docx", "4.docx", "5.doc", "6.pdf"};
    for (String file : files) {
      File f = new File(getClass().getResource("tearline/" + file).getPath());

      try (InputStream is = new FileInputStream(f); ) {
        contentExtractor.processStream(is, f.getPath(), jCas);
        assertEquals(translation, jCas.getDocumentText());

        jCas.reset();
      }
    }
    contentExtractor.destroy();
  }

  @Test
  public void testTearlineTextLeftAsOriginalIfException() throws Exception {

    String toTranslate = "This is the first tearline.";

    when(translationService.translate(eq(toTranslate))).thenThrow(TranslationException.class);

    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingTearlineContentExtractor(translationService);

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    String[] files = new String[] {"1.docx", "2.docx", "3.docx", "4.docx", "5.doc", "6.pdf"};
    for (String file : files) {
      File f = new File(getClass().getResource("tearline/" + file).getPath());

      try (InputStream is = new FileInputStream(f); ) {
        contentExtractor.processStream(is, f.getPath(), jCas);
        assertEquals(toTranslate, jCas.getDocumentText());

        jCas.reset();
      }
    }
    contentExtractor.destroy();
  }

  @Test
  public void testNoTearline() throws Exception {

    String toTranslate = "This document has no tearline.";
    String translation = "Ce document n'a pas de ligne de déchirure.";

    when(translationService.translate(eq(toTranslate))).thenReturn(translation);

    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingTearlineContentExtractor(translationService);

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());

    File f = new File(getClass().getResource("tearline/notearline.docx").getPath());

    try (InputStream is = new FileInputStream(f); ) {
      contentExtractor.processStream(is, f.getPath(), jCas);
      assertEquals(translation, jCas.getDocumentText());

      jCas.reset();
    }
    contentExtractor.destroy();
  }
}
