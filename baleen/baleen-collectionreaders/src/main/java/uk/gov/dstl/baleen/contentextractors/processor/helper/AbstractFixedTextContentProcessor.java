package uk.gov.dstl.baleen.contentextractors.processor.helper;

import java.io.IOException;

import org.apache.uima.jcas.JCas;

/**
 * Base class for content processors which can not change the document text.
 *
 *
 */
public abstract class AbstractFixedTextContentProcessor extends AbstractContentProcessor {

	@Override
	public final JCas process(JCas input, JCas clean) throws IOException {
		process(input);
		return input;
	}

	protected abstract void process(JCas input);
}
