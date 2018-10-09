// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.types.metadata.Metadata;
import uk.gov.dstl.baleen.uima.BaleenContentExtractor;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;

@RunWith(MockitoJUnitRunner.class)
public class TranslatingTikaContentExtractorTest {

  @Mock private SharedTranslationResource translationService;

  @Test
  public void testTikaWord() throws Exception {
    String toTranslate =
        "Test Document\nThis is a simple test document, with a title and a single sentence.\n";
    String translation =
        "Document de test\nCeci est un document de test simple, avec un titre et une phrase unique.\n";

    when(translationService.translate(toTranslate)).thenReturn(translation);
    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingTikaContentExtractor(translationService);

    File f = new File(getClass().getResource("test.docx").getPath());

    contentExtractor.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());
    try (InputStream is = new FileInputStream(f); ) {
      contentExtractor.processStream(is, f.getPath(), jCas);
    }
    contentExtractor.destroy();

    assertEquals(translation, jCas.getDocumentText());

    Collection<Metadata> metadata = JCasUtil.select(jCas, Metadata.class);
    assertEquals(44, metadata.size());

    Map<String, String> metadataMap = new HashMap<>();
    for (Metadata md : metadata) {
      metadataMap.put(md.getKey(), md.getValue());
    }

    assertTrue(metadataMap.containsKey("Page-Count"));
    assertEquals("1", metadataMap.get("Page-Count"));

    assertTrue(metadataMap.containsKey("meta:author"));
    assertEquals("James Baker", metadataMap.get("meta:author"));
  }
}
