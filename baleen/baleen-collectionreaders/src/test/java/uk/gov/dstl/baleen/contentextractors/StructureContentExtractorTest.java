package uk.gov.dstl.baleen.contentextractors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.UimaContextFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.tenode.baleen.extraction.Extraction;
import com.tenode.baleen.extraction.exception.ExtractionException;
import com.tenode.baleen.extraction.impl.DefaultExtraction;

import uk.gov.dstl.baleen.types.language.Text;
import uk.gov.dstl.baleen.types.metadata.Metadata;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.uima.BaleenContentExtractor;
import uk.gov.dstl.baleen.uima.testing.JCasSingleton;

public class StructureContentExtractorTest {

  public static class TestStructureContentExtractor extends StructureContentExtractor {

    @Override
    protected Extraction extract(final InputStream stream, final String source)
        throws ExtractionException {
      final Multimap<String, String> metadata = LinkedHashMultimap.create();
      metadata.put("test", "true");
      return new DefaultExtraction(
          "<html><head><meta name=\"test\" content=\"true\" /></head><body><h1>Title</h1>\n<p>Example</p></body></html>",
          metadata);
    }
  }

  @Test
  public void test() throws UIMAException, IOException {
    final UimaContext context = UimaContextFactory.createUimaContext();
    final JCas jCas = JCasSingleton.getJCasInstance();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    contentExtractor.initialize(context, Collections.emptyMap());

    contentExtractor.processStream(null, "source", jCas);

    assertEquals("Title\nExample", jCas.getDocumentText());
    final Collection<Paragraph> select = JCasUtil.select(jCas, Paragraph.class);
    assertEquals(select.size(), 1);
    final Paragraph p = select.iterator().next();
    assertEquals(p.getBegin(), 6);
    assertEquals(p.getEnd(), 13);

    final List<Metadata> contentMeta = JCasUtil.select(jCas, Metadata.class).stream()
        .filter(m -> m.getKey().startsWith("baleen:content-")).collect(Collectors.toList());
    assertEquals(3, contentMeta.size());
  }

  @Test
  public void testInitializingManipulator() throws UIMAException, IOException {
    final UimaContext context = UimaContextFactory.createUimaContext();
    final JCas jCas = JCasSingleton.getJCasInstance();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    final Map<String, Object> params = new HashMap<>();
    params.put("contentManipulators", new String[] {"RemoveEmptyText"});
    contentExtractor.initialize(context, params);

    contentExtractor.processStream(null, "source", jCas);

    final long count = JCasUtil.select(jCas, Metadata.class).stream()
        .filter(m -> m.getKey().equals("baleen:content-manipulators")
            && m.getValue().contains("RemoveEmptyText"))
        .count();
    assertEquals(1, count);
  }

  @Test
  public void testInitializingMapper() throws UIMAException, IOException {
    final UimaContext context = UimaContextFactory.createUimaContext();
    final JCas jCas = JCasSingleton.getJCasInstance();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    final Map<String, Object> params = new HashMap<>();
    params.put("contentMappers", new String[] {"MetaTags"});
    contentExtractor.initialize(context, params);

    contentExtractor.processStream(null, "source", jCas);

    final long count = JCasUtil.select(jCas, Metadata.class).stream()
        .filter(m -> m.getKey().equals("baleen:content-mappers")
            && m.getValue().contains("MetaTags"))
        .count();
    assertEquals(1, count);
  }

  @Test(expected = ResourceInitializationException.class)
  public void testInitializingBadMapper() throws UIMAException, IOException {
    final UimaContext context = UimaContextFactory.createUimaContext();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    final Map<String, Object> params = new HashMap<>();
    params.put("contentMappers", new String[] {"DoesNotExist"});
    contentExtractor.initialize(context, params);

  }

  @Test
  public void testInitializingManipulatorAsMapper() throws UIMAException, IOException {
    final UimaContext context = UimaContextFactory.createUimaContext();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    final Map<String, Object> params = new HashMap<>();
    params.put("contentMappers",
        new String[] {"uk.gov.dstl.baleen.contentmanipulators.HeaderAndFooterRemover"});
    contentExtractor.initialize(context, params);

    // TODO Could test its not actually used here...

  }

  @Test
  public void testTextBlocksEnabled() throws Exception {
    final UimaContext context = UimaContextFactory.createUimaContext();
    final JCas jCas = JCasSingleton.getJCasInstance();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    contentExtractor.initialize(context, Collections.emptyMap());

    contentExtractor.processStream(null, "source", jCas);

    assertEquals("Title\nExample", jCas.getDocumentText());
    final Collection<Text> select = JCasUtil.select(jCas, Text.class);
    assertTrue(select.size() > 0);

  }

  @Test
  public void testDisableTextBlocks() throws Exception {
    final UimaContext context = UimaContextFactory.createUimaContext();
    final JCas jCas = JCasSingleton.getJCasInstance();

    final BaleenContentExtractor contentExtractor = new TestStructureContentExtractor();
    final Map<String, Object> map = new HashMap<>();
    map.put(StructureContentExtractor.FIELD_EXTRACT_TEXT_BLOCKS, "false");
    contentExtractor.initialize(context, map);

    contentExtractor.processStream(null, "source", jCas);

    assertEquals("Title\nExample", jCas.getDocumentText());
    final Collection<Text> select = JCasUtil.select(jCas, Text.class);
    assertTrue(select.isEmpty());

  }
}
