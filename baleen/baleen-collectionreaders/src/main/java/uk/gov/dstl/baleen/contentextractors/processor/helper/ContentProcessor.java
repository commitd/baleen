package uk.gov.dstl.baleen.contentextractors.processor.helper;

import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.UimaSupport;

public interface ContentProcessor {
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
	 * Ideally this would be output = process(input) but the cost of creating
	 * JCas and issues with threading mean is provide a clean JCas which is
	 * reused.
	 *
	 * The clean JCas has no text, nor annotations. You can use it as the ouput
	 * (by copying/creating/setting up annotations and setting the text) if that
	 * makes sense in your processing.
	 *
	 * Alternatively you can just act on input (noting though you can't change
	 * the text of a JCas once set.. and then text has been set on input)
	 *
	 * @param input
	 *            The JCas containing the structure to process
	 * @param clean
	 *            A clean empty JCas which can ignored, used a temporary JCas to
	 *            store information in, or used to copy values from intput to
	 * @return the processed JCas output (either input or clean parameter)
	 *
	 */
	JCas process(JCas input, JCas clean) throws IOException;

	/**
	 * Destroy the ContentProcessor
	 */
	void destroy();
}
