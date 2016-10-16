//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.contentextractors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.tenode.baleen.extraction.Extraction;
import com.tenode.baleen.extraction.FormatExtractor;
import com.tenode.baleen.extraction.exception.ExtractionException;
import com.tenode.baleen.extraction.tika.Tag;
import com.tenode.baleen.extraction.tika.TikaFormatExtractor;

import uk.gov.dstl.baleen.contentextractors.helpers.AbstractContentExtractor;
import uk.gov.dstl.baleen.contentextractors.helpers.SimpleTagToStructureMapper;
import uk.gov.dstl.baleen.contentextractors.processor.IContentProcessor;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public List<IContentProcessor> createContentProcessors(UimaContext context, String[] contentProcesors)
			throws InvalidParameterException {
		List<IContentProcessor> processors = new ArrayList<>();
		for (String contentProcesor : contentProcesors) {
			IContentProcessor processor;
			try {
				processor = (IContentProcessor) CpeBuilderUtils
						.getClassFromString(contentProcesor, CONTENT_PROCESSOR_DEFAULT_PACKAGE).newInstance();

				processor.initialize(context, getSupport(), getMonitor()); // Consider params
				processors.add(processor);
			} catch (InstantiationException | IllegalAccessException | ResourceInitializationException e) {
				LOGGER.info("Could not find or instantiate content processor " + contentProcesor, e);
			}

		}
		return processors;
	}

	@Override
	public void doProcessStream(InputStream stream, String source, JCas jCas) throws IOException {
		super.doProcessStream(stream, source, jCas);

		try {
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();

			FormatExtractor formatExtractor = new TikaFormatExtractor(new AutoDetectParser());

			Extraction extraction = formatExtractor.parse(stream, metadata, context);

			JCas input = JCasFactory.createJCas();

			input.setDocumentText(extraction.getText());

			SimpleTagToStructureMapper mapper = new SimpleTagToStructureMapper(input);
			for (Tag tag : extraction.getTags()) {
				Optional<Structure> structure = mapper.map(tag);
				structure.ifPresent(this::addToJCasIndex);
			}

			for (IContentProcessor processor : processors) {
				JCas ouput = JCasFactory.createJCas();
				processor.process(input, ouput);
				input = ouput;
			}

			CasCopier.copyCas(input.getCas(), jCas.getCas(), true);

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
