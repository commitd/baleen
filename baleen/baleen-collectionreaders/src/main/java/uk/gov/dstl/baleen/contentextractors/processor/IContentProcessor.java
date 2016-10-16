package uk.gov.dstl.baleen.contentextractors.processor;

import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.UimaSupport;

public interface IContentProcessor {
	/**
	 * Initialize the ContentExtractor
	 *
	 * @param context
	 */
	void initialize(UimaContext context, UimaSupport uimaSupport, UimaMonitor uimaMonitor)
			throws ResourceInitializationException;

	/**
	 * Process the content stored in the given JCas
	 *
	 * @param input
	 *            The JCas containing the structure to process
	 * @param ouput
	 *            The JCas to transform to
	 */
	void process(JCas input, JCas ouput) throws IOException;

	/**
	 * Destroy the ContentProcessor
	 */
	void destroy();
}
