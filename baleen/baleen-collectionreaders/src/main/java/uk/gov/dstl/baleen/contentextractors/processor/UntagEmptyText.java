package uk.gov.dstl.baleen.contentextractors.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.contentextractors.processor.helper.AbstractFixedTextContentProcessor;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;

public class UntagEmptyText extends AbstractFixedTextContentProcessor {

	private final List<Class<? extends Structure>> TEXT_TYPES = Arrays.asList(Paragraph.class, Caption.class,
			Header.class, Footer.class, Link.class, Style.class);

	@Override
	public void process(JCas input) {
		final List<Structure> toRemove = new ArrayList<>();

		for (final Structure s : JCasUtil.select(input, Structure.class)) {
			if (TEXT_TYPES.contains(s.getClass()) && s.getBegin() == s.getEnd()) {
				toRemove.add(s);
			}
		}

		getSupport().remove(toRemove);
	}

}
