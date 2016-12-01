// Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tika.metadata.Metadata;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
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
import uk.gov.dstl.baleen.contentextractors.processor.ContentProcessor;
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

	private List<ContentProcessor> processors;

	@Override
	public void doInitialize(UimaContext context, Map<String, Object> params) throws ResourceInitializationException {
		super.doInitialize(context, params);

		final Object config = params.get("contentProcessors");
		if (config != null && config instanceof String[]) {
			try {
				processors = createContentProcessors(context, (String[]) config);
			} catch (final InvalidParameterException e) {
				throw new ResourceInitializationException(e);
			}
		}

	}

	private List<ContentProcessor> createContentProcessors(UimaContext context, String[] contentProcesors)
			throws InvalidParameterException {
		final List<ContentProcessor> newProcessors = new ArrayList<>();
		for (final String contentProcesor : contentProcesors) {
			try {
				final ContentProcessor processor = (ContentProcessor) CpeBuilderUtils
						.getClassFromString(contentProcesor, CONTENT_PROCESSOR_DEFAULT_PACKAGE).newInstance();

				processor.initialize(context, getSupport(), getMonitor()); // Consider
																			// params
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
			final TikaFormatExtractor formatExtractor = new TikaFormatExtractor();

			final TikaExtraction extraction = formatExtractor.parse(stream, source);

			final JCas structuredJCas = JCasFactory.createJCas();
			structuredJCas.setDocumentText(extraction.getText());

			final SimpleTagToStructureMapper mapper = new SimpleTagToStructureMapper(structuredJCas,
					extraction.getMetadata());
			extraction.getTags().stream().map(mapper::map).filter(Optional::isPresent).map(Optional::get)
					.forEach(Annotation::addToIndexes);

			JCas manipulatedJCas;
			if (processors == null || processors.isEmpty()) {
				manipulatedJCas = structuredJCas;
			} else {
				JCas input = structuredJCas;
				JCas output = JCasFactory.createJCas();
				for (final ContentProcessor processor : processors) {
					output.reset();
					processor.process(input, output);
					final JCas tmp = input;
					input = output;
					output = tmp;
				}
				manipulatedJCas = input;
			}

			// JCasUtil.select(manipulatedJCas, Heading.class).stream()
			// .forEach(h -> System.out.println(h.getLevel()));
			CasCopier.copyCas(manipulatedJCas.getCas(), jCas.getCas(), true);
			// JCasUtil.select(jCas, Heading.class).stream().forEach(h ->
			// System.out.println(h.getLevel()));

			super.doProcessStream(stream, source, jCas);

			final Metadata metadata = extraction.getMetadata();
			for (final String name : metadata.names()) {
				addMetadata(jCas, name, metadata.get(name));
			}
		} catch (final UIMAException e) {
			getMonitor().warn("Couldn't process document from '{}'", source, e);
			setCorrupt(jCas);
		} catch (final ExtractionException e) {
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
