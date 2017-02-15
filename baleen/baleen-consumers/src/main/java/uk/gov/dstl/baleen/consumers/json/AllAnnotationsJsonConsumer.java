package uk.gov.dstl.baleen.consumers.json;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.types.BaleenAnnotation;

public class AllAnnotationsJsonConsumer extends AbstractJsonConsumer {

	@Override
	protected Iterable<BaleenAnnotation> selectAnnotations(JCas jCas) {
		return JCasUtil.select(jCas, BaleenAnnotation.class);
	}

}