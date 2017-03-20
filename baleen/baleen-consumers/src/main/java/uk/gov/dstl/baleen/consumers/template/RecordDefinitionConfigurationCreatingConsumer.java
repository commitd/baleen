package uk.gov.dstl.baleen.consumers.template;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dstl.baleen.consumers.utils.SourceUtils;
import uk.gov.dstl.baleen.cpe.CpeBuilderUtils;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Aside;
import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.DefinitionDescription;
import uk.gov.dstl.baleen.types.structure.DefinitionItem;
import uk.gov.dstl.baleen.types.structure.DefinitionList;
import uk.gov.dstl.baleen.types.structure.Details;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Page;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Preformatted;
import uk.gov.dstl.baleen.types.structure.Quotation;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sentence;
import uk.gov.dstl.baleen.types.structure.Sheet;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.SlideShow;
import uk.gov.dstl.baleen.types.structure.SpreadSheet;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Summary;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableFooter;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;
import uk.gov.dstl.baleen.types.structure.TextDocument;
import uk.gov.dstl.baleen.types.structure.Unordered;
import uk.gov.dstl.baleen.types.templates.RecordDefinition;
import uk.gov.dstl.baleen.types.templates.TemplateFieldDefinition;
import uk.gov.dstl.baleen.uima.BaleenConsumer;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

public class RecordDefinitionConfigurationCreatingConsumer extends BaleenConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordDefinitionConfigurationCreatingConsumer.class);

	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	private static final Class<?>[] DEFAULT_STRUCTURAL_CLASSES = { Anchor.class, Aside.class, Caption.class,
			DefinitionDescription.class, DefinitionItem.class, DefinitionList.class, Details.class, SlideShow.class,
			Document.class, SpreadSheet.class, TextDocument.class, Figure.class, Footer.class, Header.class,
			Heading.class, Link.class, ListItem.class, Ordered.class, Page.class, Sheet.class, Slide.class,
			Paragraph.class, Preformatted.class, Quotation.class, Section.class, Sentence.class, Style.class,
			Summary.class, Table.class, TableBody.class, TableCell.class, TableFooter.class, TableHeader.class,
			TableRow.class, Unordered.class };

	/**
	 * A list of structural types which will be considered during record path
	 * analysis.
	 *
	 * @baleen.config Paragraph,TableCell,ListItem,Aside, ...
	 */
	public static final String PARAM_TYPE_NAMES = "types";
	@ConfigurationParameter(name = PARAM_TYPE_NAMES, mandatory = false)
	private String[] typeNames;

	private Set<Class<? extends Structure>> structuralClasses;

	public static final String PARAM_OUTPUT_FILE = "outputDirectory";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, defaultValue = "recordDefinitions")
	private String outputDirectory = "recordDefinitions";

	@SuppressWarnings("unchecked")
	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		structuralClasses = new HashSet<>();
		if (typeNames == null || typeNames.length == 0) {
			for (Class<?> clazz : DEFAULT_STRUCTURAL_CLASSES) {
				structuralClasses.add((Class<? extends Structure>) clazz);
			}
		} else {
			for (final String typeName : typeNames) {
				try {
					structuralClasses.add(CpeBuilderUtils.getClassFromString(typeName, DEFAULT_STRUCTURAL_PACKAGE));
				} catch (final InvalidParameterException e) {
					throw new ResourceInitializationException(e);
				}
			}
		}
	}

	@Override
	protected void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
		Collection<RecordDefinitionConfiguration> definitions = new ArrayList<>();

		Collection<RecordDefinition> recordDefinitions = JCasUtil.select(jCas, RecordDefinition.class);
		for (RecordDefinition recordDefinition : recordDefinitions) {

			List<TemplateFieldDefinition> fields = JCasUtil.selectCovered(TemplateFieldDefinition.class,
					recordDefinition);
			Map<String, String> fieldPaths = new HashMap<>();

			for (TemplateFieldDefinition templateFieldDefinition : fields) {
				String fieldPath = SelectorUtils.generatePath(recordDefinition, templateFieldDefinition,
						structuralClasses);
				fieldPaths.put(templateFieldDefinition.getName(), fieldPath);
			}
			definitions.add(new RecordDefinitionConfiguration(recordDefinition.getName(), fieldPaths));
		}

		String documentSourceName = SourceUtils.getDocumentSourceBaseName(jCas, getSupport());
		try (Writer w = createOutputWriter(documentSourceName)) {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			mapper.writeValue(w, definitions);
		} catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private Writer createOutputWriter(final String documentSourceName) throws IOException {
		Path directoryPath = Paths.get(outputDirectory);
		if (!Files.exists(directoryPath)) {
			Files.createDirectories(directoryPath);
		}
		String baseName = FilenameUtils.getBaseName(documentSourceName);
		Path outputFilePath = directoryPath.resolve(baseName + ".yml");

		if (Files.exists(outputFilePath)) {
			LOGGER.warn("Overwriting existing output properties file {}", outputFilePath.toString());
		}
		return Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8);
	}

}