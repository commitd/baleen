package uk.gov.dstl.baleen.contentextractors.processor;

import java.util.Collection;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.contentextractors.processor.helper.AbstractRegionRemovingContentProcessor;
import uk.gov.dstl.baleen.contentextractors.processor.helper.AbstractRegionRemovingContentProcessor.Span;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;

public class HeaderAndFooterRemover extends AbstractRegionRemovingContentProcessor {

	@Override
	protected Collection<Span> findSpansToRemove(JCas input) {

		final Collection<Span> headerSpans = toSpan(JCasUtil.select(input, Header.class));
		final Collection<Span> footerSpans = toSpan(JCasUtil.select(input, Footer.class));

		return combineSpans(headerSpans, footerSpans);
	}

}
