// Copyright (c) Committed Software 2018, opensource@committed.io
package uk.gov.dstl.baleen.contentextractor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import io.committed.krill.extraction.Extraction;
import io.committed.krill.extraction.exception.ExtractionException;
import io.committed.krill.extraction.impl.DefaultExtraction;

import uk.gov.dstl.baleen.resources.SharedTranslationResource;
import uk.gov.dstl.baleen.translation.TranslationException;
import uk.gov.dstl.baleen.types.metadata.Metadata;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.uima.BaleenContentExtractor;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;

@RunWith(MockitoJUnitRunner.class)
public class TranslatingStructureContentExtractorTest {

  public static class TestContentExtractor extends TranslatingStructureContentExtractor {

    public TestContentExtractor(SharedTranslationResource translationService) {
      super(translationService);
    }

    @Override
    protected Extraction extract(InputStream stream, String source) throws ExtractionException {
      Multimap<String, String> metadata = LinkedHashMultimap.create();
      metadata.put("test", "true");
      return new DefaultExtraction(
          "<html><head><meta name=\"test\" content=\"true\" /></head><body><h1>Title</h1>\n<p>Example</p></body></html>",
          metadata);
    }
  }

  @Mock private SharedTranslationResource translationService;

  @Test
  public void testTranslates() throws Exception {

    String toTranslate = "Title\nExample";
    String firstTranslate = "Title";
    String secondTranslate = "Example";
    String firstTranslation = "De titre";
    String secondTranslation = "Exemple";
    String translation = "De titre\nExemple";

    when(translationService.translate("\n")).thenReturn("\n");
    when(translationService.translate(firstTranslate)).thenReturn(firstTranslation);
    when(translationService.translate(secondTranslate)).thenReturn(secondTranslation);

    UimaContext context = UimaContextFactory.createUimaContext();
    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor = new TestContentExtractor(translationService);
    contentExtractor.initialize(context, Collections.emptyMap());

    contentExtractor.processStream(null, "source", jCas);

    assertEquals(translation, jCas.getDocumentText());
    Collection<Paragraph> select = JCasUtil.select(jCas, Paragraph.class);
    assertEquals(select.size(), 1);
    Paragraph p = select.iterator().next();
    assertEquals(p.getBegin(), 9);
    assertEquals(p.getEnd(), 16);

    List<Metadata> contentMeta =
        JCasUtil.select(jCas, Metadata.class)
            .stream()
            .filter(m -> m.getKey().startsWith("baleen:content-"))
            .collect(Collectors.toList());
    assertEquals(3, contentMeta.size());
  }

  @Test
  public void testLeavesOriginalIfError() throws Exception {

    String toTranslate = "Title\nExample";

    when(translationService.translate(anyString())).thenThrow(TranslationException.class);

    UimaContext context = UimaContextFactory.createUimaContext();
    JCas jCas = JCasSingleton.getJCasInstance();

    BaleenContentExtractor contentExtractor = new TestContentExtractor(translationService);
    contentExtractor.initialize(context, Collections.emptyMap());

    contentExtractor.processStream(null, "source", jCas);

    assertEquals(toTranslate, jCas.getDocumentText());
    Collection<Paragraph> select = JCasUtil.select(jCas, Paragraph.class);
    assertEquals(select.size(), 1);
    Paragraph p = select.iterator().next();
    assertEquals(p.getBegin(), 6);
    assertEquals(p.getEnd(), 13);

    List<Metadata> contentMeta =
        JCasUtil.select(jCas, Metadata.class)
            .stream()
            .filter(m -> m.getKey().startsWith("baleen:content-"))
            .collect(Collectors.toList());
    assertEquals(3, contentMeta.size());
  }
}
