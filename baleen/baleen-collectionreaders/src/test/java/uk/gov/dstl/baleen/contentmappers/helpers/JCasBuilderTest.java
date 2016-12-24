package uk.gov.dstl.baleen.contentmappers.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import uk.gov.dstl.baleen.types.Base;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.structure.Paragraph;

public class JCasBuilderTest {

  @Test
  public void testBuild() throws UIMAException {
    final JCas jCas = JCasFactory.createJCas();

    final JCasBuilder builder = new JCasBuilder(jCas);

    assertSame(jCas, builder.getJCas());

    final int s = builder.getCurrentOffset();
    assertEquals(s, 0);
    builder.addText("Hello");
    final int e = builder.getCurrentOffset();
    assertEquals(e, "Hello".length());

    builder.addAnnotations(Arrays.asList(new Entity(jCas), new Paragraph(jCas)), s, e, 6);

    builder.build();

    assertEquals(jCas.getDocumentText(), "Hello");
    final Collection<Base> entities = JCasUtil.select(jCas, Base.class);
    assertEquals(entities.size(), 2);
    final Iterator<Base> iterator = entities.iterator();
    final Annotation a = iterator.next();
    final Annotation b = iterator.next();
    Entity entity;
    Paragraph paragraph;
    if (a instanceof Entity) {
      entity = (Entity) a;
      paragraph = (Paragraph) b;
    } else {
      entity = (Entity) b;
      paragraph = (Paragraph) a;
    }

    assertEquals(entity.getBegin(), 0);
    assertEquals(entity.getEnd(), 5);
    assertEquals(paragraph.getBegin(), 0);
    assertEquals(paragraph.getEnd(), 5);
    assertEquals(paragraph.getDepth(), 6);

  }

}
