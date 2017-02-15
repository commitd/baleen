package uk.gov.dstl.baleen.consumers.json;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import uk.gov.dstl.baleen.types.templates.TemplateField;

public class TemplateFieldJsonReportConsumer extends AbstractJsonConsumer {

	@Override
	protected Iterable<TemplateField> selectAnnotations(JCas jCas) {
		return JCasUtil.select(jCas, TemplateField.class);
	}

}