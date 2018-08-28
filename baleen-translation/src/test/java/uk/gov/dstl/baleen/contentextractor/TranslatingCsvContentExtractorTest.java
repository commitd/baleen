// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.dstl.baleen.contentextractors.CsvContentExtractor;
import uk.gov.dstl.baleen.contentextractors.CsvContentExtractorTest;
import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.types.metadata.Metadata;
import uk.gov.dstl.baleen.uima.BaleenContentExtractor;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;

@RunWith(MockitoJUnitRunner.class)
public class TranslatingCsvContentExtractorTest {

  @Mock private SharedTranslationResource translationService;

  @Test
  public void testTranslates() throws Exception {

    String toTranslate = "Hello world, my name is John Smith";
    String translation = "Bonjour tout le monde, je m'appelle John Smith";

    when(translationService.translate(toTranslate)).thenReturn(translation);

    UimaContext context = UimaContextFactory.createUimaContext();
    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor =
        new TranslatingCsvContentExtractor(translationService);

    File f = new File(CsvContentExtractorTest.class.getResource("test.csv").getPath());

    Map<String, Object> config = new HashMap<>();
    config.put(CsvContentExtractor.PARAM_SEPARATOR, ",");
    config.put(CsvContentExtractor.PARAM_CONTENT_COLUMN, "2");
    config.put(CsvContentExtractor.PARAM_COLUMNS, Arrays.asList("id", "test1", "", "test3"));

    contentExtractor.initialize(context, config);
    try (InputStream is = new FileInputStream(f); ) {
      contentExtractor.processStream(is, f.getPath(), jCas);
    }
    contentExtractor.destroy();

    assertEquals(translation, jCas.getDocumentText());

    Collection<Metadata> metadata = JCasUtil.select(jCas, Metadata.class);
    assertEquals(6, metadata.size());

    Map<String, String> metadataMap = new HashMap<>();
    for (Metadata md : metadata) {
      metadataMap.put(md.getKey(), md.getValue());
    }

    assertTrue(metadataMap.containsKey("id"));
    assertEquals("43", metadataMap.get("id"));

    assertTrue(metadataMap.containsKey("test1"));
    assertEquals("Foo", metadataMap.get("test1"));

    assertTrue(metadataMap.containsKey("column4"));
    assertEquals("Bar", metadataMap.get("column4"));

    assertTrue(metadataMap.containsKey("test3"));
    assertEquals("Baz", metadataMap.get("test3"));

    assertTrue(metadataMap.containsKey("column6"));
    assertEquals("12345", metadataMap.get("column6"));
  }
}
