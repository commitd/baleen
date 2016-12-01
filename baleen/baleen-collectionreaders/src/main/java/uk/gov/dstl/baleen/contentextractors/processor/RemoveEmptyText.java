package uk.gov.dstl.baleen.contentextractors.processor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;

public class RemoveEmptyText extends AbstractContentProcessor {

  private final List<Class<? extends Structure>> TEXT_TYPES = Arrays.asList(Paragraph.class,
      Caption.class, Header.class, Footer.class, Link.class, Style.class);

  @Override
  public void process(JCas input, JCas ouput) throws IOException {
    ouput.setDocumentText(input.getDocumentText());

    final List<Structure> structures = new ArrayList<>(JCasUtil.select(input, Structure.class));
    for (final Structure s : structures) {
      if (!(TEXT_TYPES.contains(s.getClass()) && s.getBegin() == s.getEnd())) {
        copy(ouput, s);
      }
    }

  }

  public void copy(JCas ouput, Structure s) {
    Constructor<? extends Structure> constructor;
    try {
      constructor = s.getClass().getConstructor(JCas.class, int.class, int.class);
      getSupport().add(constructor.newInstance(ouput, s.getBegin(), s.getEnd()));
    } catch (NoSuchMethodException | SecurityException | InstantiationException
        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      getMonitor().warn("Can not copy Structure {}", s);
    }
  }
}
