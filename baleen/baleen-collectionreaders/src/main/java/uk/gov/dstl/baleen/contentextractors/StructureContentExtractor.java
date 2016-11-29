//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tika.metadata.Metadata;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.tenode.baleen.extraction.exception.ExtractionException;
import com.tenode.baleen.extraction.tika.TikaExtraction;
import com.tenode.baleen.extraction.tika.TikaFormatExtractor;

import uk.gov.dstl.baleen.contentextractors.helpers.AbstractContentExtractor;
import uk.gov.dstl.baleen.contentextractors.helpers.SimpleTagToStructureMapper;
import uk.gov.dstl.baleen.contentextractors.processor.IContentProcessor;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;

/**
 * Extracts metadata, structure and text content from the supplied input.
 */
public class StructureContentExtractor extends AbstractContentExtractor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(StructureContentExtractor.class);

	public static final String CORRUPT_FILE_TEXT = "FILE CONTENTS CORRUPT - UNABLE TO PROCESS";

	/** The Constant CONTENT_EXTRATOR_DEFAULT_PACKAGE. */
	public static final String CONTENT_PROCESSOR_DEFAULT_PACKAGE = "uk.gov.dstl.baleen.contentextractors.processor";

	private List<IContentProcessor> processors;

	@Override
	public void doInitialize(UimaContext context, Map<String, Object> params) throws ResourceInitializationException {
		super.doInitialize(context, params);

		Object config = params.get("contentProcessors");
		if (config != null && config instanceof String[]) {
			try {
				processors = createContentProcessors(context, (String[]) config);
			} catch (InvalidParameterException e) {
			  throw new ResourceInitializationException(e);
			}
		}

	}

	private List<IContentProcessor> createContentProcessors(UimaContext context, String[] contentProcesors)
			throws InvalidParameterException {
		List<IContentProcessor> newProcessors = new ArrayList<>();
		for (String contentProcesor : contentProcesors) {
			try {
				IContentProcessor processor = (IContentProcessor) CpeBuilderUtils
						.getClassFromString(contentProcesor, CONTENT_PROCESSOR_DEFAULT_PACKAGE).newInstance();

				processor.initialize(context, getSupport(), getMonitor()); // Consider params
				newProcessors.add(processor);
			} catch (InstantiationException | IllegalAccessException | ResourceInitializationException e) {
				LOGGER.info("Could not find or instantiate content processor " + contentProcesor, e);
			}
		}
		return newProcessors;
	}

	@Override
	public void doProcessStream(InputStream stream, String source, JCas jCas) throws IOException {

		try {
			TikaFormatExtractor formatExtractor = new TikaFormatExtractor();

			TikaExtraction extraction = formatExtractor.parse(stream, source);

			JCas input = JCasFactory.createJCas();
			input.setDocumentText(extraction.getText());

			SimpleTagToStructureMapper mapper = new SimpleTagToStructureMapper(input, extraction.getMetadata());
			extraction.getTags().stream().map(mapper::map).forEach(s -> s.ifPresent(this::addToJCasIndex));

			JCas output = JCasFactory.createJCas();
			for (IContentProcessor processor : processors) {
				processor.process(input, output);
				JCas tmp = input;
				tmp.reset();
				input = output;
				output = tmp;
			}

			CasCopier.copyCas(input.getCas(), jCas.getCas(), true);
	        super.doProcessStream(stream, source, jCas);

			Metadata metadata = extraction.getMetadata();
			for (String name : metadata.names()) {
				addMetadata(jCas, name, metadata.get(name));
			}
		} catch (UIMAException e) {
			getMonitor().warn("Couldn't process document from '{}'", source, e);
			setCorrupt(jCas);
		} catch (ExtractionException e) {
			getMonitor().warn("Couldn't extract structure from document '{}'", source, e);
			setCorrupt(jCas);
		}
	}

	private void setCorrupt(JCas jCas) {
		if (Strings.isNullOrEmpty(jCas.getDocumentText())) {
			jCas.setDocumentText(CORRUPT_FILE_TEXT);
		}
	}

}
