package uk.gov.dstl.baleen.annotators.templates;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import uk.gov.dstl.baleen.exceptions.InvalidParameterException;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.templates.TemplateField;
import uk.gov.dstl.baleen.uima.BaleenAnnotator;
import uk.gov.dstl.baleen.uima.utils.SelectorUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Annotates template fields found in documents using configurable selector
 * queries.
 * <p>
 * The current syntax follows a very limited subset of CSS selectors. The only
 * operators supported are immediate children (&gt;) and the pseudo-selector
 * "nth-of-type(n)".
 * </p>
 * <p>
 * This class reads selectors from properties file. Each file should have one or
 * more key-value pairs, where the key is the name of the field and value is the
 * selector.
 * </p>
 * 
 * @see SelectorUtils
 */
public class TemplateSelectorAnnotator extends BaleenAnnotator {

	/** The package used to resolve relative structural type names. */
	private static final String DEFAULT_STRUCTURAL_PACKAGE = "uk.gov.dstl.baleen.types.structure";

	/** The selectors directory parameter. */
	public static final String PARAM_SELECTORS_DIRECTORY = "selectorsDirectory";

	/** The output directory for template selector property files */
	@ConfigurationParameter(name = PARAM_SELECTORS_DIRECTORY, defaultValue = "templateSelectors")
	private String selectorsDirectory = "templateSelectors";

	/** Collected field selectors. */
	private Map<String, String> selectors = new HashMap<>();

	@Override
	public void doInitialize(UimaContext aContext) throws ResourceInitializationException {
		super.doInitialize(aContext);
		readSelectors();
	}

	/**
	 * Read selectors.
	 *
	 * @throws ResourceInitializationException
	 *             the resource initialization exception
	 */
	private void readSelectors() throws ResourceInitializationException {
		final Path path = Paths.get(selectorsDirectory);
		try {
			Files.list(path).filter(Files::isRegularFile).forEach(this::readSelectorsFromPath);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	/**
	 * Read selectors from path.
	 *
	 * @param path
	 *            the path
	 */
	private void readSelectorsFromPath(final Path path) {
		final Properties selectorsFromPath = new Properties();
		try {
			selectorsFromPath.load(Files.newBufferedReader(path, StandardCharsets.UTF_8));
			selectorsFromPath.stringPropertyNames().forEach(p -> selectors.put(p, selectorsFromPath.getProperty(p)));
		} catch (IOException e) {
			getMonitor().warn("Failed to read from selectors file " + path, e);
		}
	}

	@Override
	protected void doProcess(JCas jCas) throws AnalysisEngineProcessException {
		for (Entry<String, String> selector : selectors.entrySet()) {
			List<? extends Structure> selected;
			try {
				selected = SelectorUtils.select(jCas, selector.getValue(), DEFAULT_STRUCTURAL_PACKAGE);
				selected.forEach(s -> addAnnotation(jCas, selector.getKey(), s));
			} catch (InvalidParameterException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}

	/**
	 * Creates and adds a TemplateField annotation with the given name, using
	 * the whole enclosing structural element as the content.
	 *
	 * @param jCas
	 *            the j cas
	 * @param name
	 *            the name
	 * @param enclosingElement
	 *            the element
	 */
	private void addAnnotation(JCas jCas, String name, Structure enclosingElement) {
		TemplateField templateField = new TemplateField(jCas);
		templateField.setName(name);
		templateField.setBegin(enclosingElement.getBegin());
		templateField.setEnd(enclosingElement.getEnd());
		templateField.setConfidence(1d);
		addToJCasIndex(templateField);
	}

}
