//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.annotators.gazetteer.helpers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.ahocorasick.trie.Trie.TrieBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CommonArrayFS;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Strings;

import uk.gov.dstl.baleen.exceptions.BaleenException;
import uk.gov.dstl.baleen.resources.gazetteer.IGazetteer;
import uk.gov.dstl.baleen.types.BaleenAnnotation;
import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.data.TextBlock;
import uk.gov.dstl.baleen.uima.utils.TypeSystemSingleton;
import uk.gov.dstl.baleen.uima.utils.TypeUtils;

/**
 * Abstract class implementing the a gazetteer using the Aho-Corasick algorithm.
 *
 * Reflection is used to try and identify entity properties and set them based on additional data
 * fields in the gazetteer. This means that this annotator can be used for any entity type, though
 * there is a risk that a malformed gazetteer could corrupt the entities.
 *
 * @baleen.javadoc
 */
public abstract class AbstractAhoCorasickAnnotator extends BaleenTextAwareAnnotator {

	/**
	 * Should comparisons be done case sensitively?
	 *
	 * @baleen.config false
	 */
	public static final String PARAM_CASE_SENSITIVE = "caseSensitive";
	@ConfigurationParameter(name = PARAM_CASE_SENSITIVE, defaultValue = "false")
	protected boolean caseSensitive;

	/**
	 * Should whitespace in document be preserved?
	 *
	 * If set to false, the document text is normalized prior to comparison, so that any sequence of
	 * whitespace characters is translated to a single space character before matching against the
	 * gazetteer. The document text in the CAS is not modified, and any annotations created will
	 * cover the correct span (including any ignored whitespace) of surface text.
	 *
	 * @baleen.config true
	 */
	public static final String PARAM_EXACT_WHITESPACE = "exactWhitespace";
	@ConfigurationParameter(name = PARAM_EXACT_WHITESPACE, defaultValue = "true")
	protected boolean exactWhitespace;

	/**
	 * The type to use for extracted entities
	 *
	 * @baleen.config Entity
	 */
	public static final String PARAM_TYPE = "type";
	@ConfigurationParameter(name = PARAM_TYPE, defaultValue = "Entity")
	protected String type;
	
	/**
	 * The subtype to use for extracted entities
	 *
	 * @baleen.config
	 */
	public static final String PARAM_SUBTYPE = "subtype";
	@ConfigurationParameter(name = PARAM_SUBTYPE, defaultValue = "")
	protected String subtype;

	protected IGazetteer gazetteer;
	protected Class<? extends Annotation> entityType;
	protected Trie trie;

	private static final String ERROR_CANT_ASSIGN_ENTITY_PROPERTY = "Unable to assign property on entity - property will be skipped";

	/**
	 * Constructor
	 *
	 * @param logger
	 *            The Logger to use for errors, etc.
	 */
	public AbstractAhoCorasickAnnotator() {
	}

	/**
	 * Configure a gazetteer object and initialise it. Remember that the caseSensitive and type
	 * properties may also need to be passed to the gazetteer, dependent on the gazetteer.
	 *
	 * @return A initialised gazetteer implementing IGazetteer
	 */
	public abstract IGazetteer configureGazetteer() throws BaleenException;

	@Override
	public void doInitialize(final UimaContext aContext) throws ResourceInitializationException {
		try {
			gazetteer = configureGazetteer();
		} catch (final BaleenException be) {
			throw new ResourceInitializationException(be);
		}

		buildTrie();

		try {
			entityType = (Class<? extends Annotation>) TypeUtils.getType(type, JCasFactory.createJCas(TypeSystemSingleton.getTypeSystemDescriptionInstance()));
			if (entityType == null) {
				getMonitor().warn("Type {} not found, Entity will be used instead", type);
				entityType = Entity.class;
			}
		} catch (final UIMAException e) {
			throw new ResourceInitializationException(e);
		}
	}

	/**
	 * Build the Trie and set the <em>trie</em> variable. This method can be overridden if you want
	 * to modify the gazetteer before parsing it.
	 */
	protected void buildTrie() {
		TrieBuilder builder = Trie.builder().onlyWholeWords();

		if (!caseSensitive) {
			builder = builder.caseInsensitive();
		}

		for (final String s : gazetteer.getValues()) {
			builder = builder.addKeyword(s);
		}

		trie = builder.build();
	}
	
	@Override
	protected final void doProcess(final JCas jCas) throws AnalysisEngineProcessException {
    	// Final so as to prevent other implementation being non text aware
    	super.doProcess(jCas);
	}

	@Override
	public void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {
		final Map<String, List<BaleenAnnotation>> entities = exactWhitespace ? processExactWhitespace(block)
				: processNormalisedWhitespace(block);

		createReferenceTargets(block, entities.values());

	}

	private Map<String, List<BaleenAnnotation>> processExactWhitespace(final TextBlock block) {
		final Map<String, List<BaleenAnnotation>> entities = new HashMap<>();

		final Collection<Emit> emits = trie.parseText(block.getCoveredText());

		for (final Emit emit : emits) {
			try {
				final String match = block.getDocumentText().substring(emit.getStart(), emit.getEnd() + 1);
				createEntityAndAliases(block, emit.getStart(), emit.getEnd() + 1, match, match, entities);
			} catch (final BaleenException be) {
				getMonitor().error("Unable to create entity of type {} for value '{}'", entityType.getName(),
						emit.getKeyword(), be);
				continue;
			}
		}

		return entities;
	}

	private Map<String, List<BaleenAnnotation>> processNormalisedWhitespace(final TextBlock block) {
		final Map<String, List<BaleenAnnotation>> entities = new HashMap<>();

		final TransformedString norm = normaliseString(block.getCoveredText());
		final Collection<Emit> emits = trie.parseText(norm.getTransformedString());

		for (final Emit emit : emits) {
			try {
				final Integer start = norm.getMapping().get(emit.getStart());
				final Integer end = norm.getMapping().get(emit.getEnd() + 1);
				final String match = norm.getOriginalString().substring(start, end);

				createEntityAndAliases(block, start, end, match, match, entities);
			} catch (final BaleenException be) {
				getMonitor().error("Unable to create entity of type {} for value '{}'", entityType.getName(),
						emit.getKeyword(), be);
				continue;
			}
		}

		return entities;
	}

	protected void createEntityAndAliases(final TextBlock block, final Integer start, final Integer end, final String value, final String aliasKey,
			final Map<String, List<BaleenAnnotation>> entities) throws BaleenException {
		final BaleenAnnotation ent = createEntity(block, start, end, value, aliasKey);

		final List<String> aliases = new ArrayList<>(Arrays.asList(gazetteer.getAliases(aliasKey)));
		aliases.add(aliasKey);

		final String key = generateKey(aliases);

		final List<BaleenAnnotation> groupEntities = entities.containsKey(key) ? entities.get(key) : new ArrayList<>();
		groupEntities.add(ent);
		entities.put(key, groupEntities);
	}

	/**
	 * Generate a key for an alias set by ordering and joining them
	 *
	 * @param aliases
	 * @return
	 */
	protected String generateKey(final List<String> aliases) {
		List<String> correctCaseAliases;

		if (!caseSensitive) {
			correctCaseAliases = aliases.stream().map(String::toLowerCase).collect(Collectors.toList());
		} else {
			correctCaseAliases = aliases;
		}

		Collections.sort(correctCaseAliases);

		return StringUtils.join(correctCaseAliases, "|");
	}

	/**
	 * Create a new entity of the configured type
	 *
	 * @param block
	 *            JCas object in which to create the entity
	 * @param begin
	 *            The beginning of the entity in the text
	 * @param end
	 *            The end of the entity in the text
	 * @param value
	 *            The value of the entity
	 * @param gazetteerKey
	 *            The key as it appears in the gazetteer
	 * @throws Exception
	 */
	protected BaleenAnnotation createEntity(final TextBlock block, final int begin, final int end, final String value, final String gazetteerKey)
			throws BaleenException {
		BaleenAnnotation ent;
		try {
			ent = (BaleenAnnotation) block.newAnnotation(entityType, begin, end);
		} catch (final Exception e) {
			throw new BaleenException("Could not create new entity", e);
		}

		if (ent instanceof Entity) {
			((Entity) ent).setValue(value);
			((Entity) ent).setConfidence(1.0);
			
			if(!Strings.isNullOrEmpty(subtype))
				((Entity) ent).setSubType(subtype);
		}

		final Map<String, Object> additionalData = gazetteer.getAdditionalData(gazetteerKey);

		if (additionalData != null && !additionalData.isEmpty()) {
			for (final Method m : entityType.getMethods()) {
				setProperty(ent, m, additionalData);
			}
		}

		addToJCasIndex(ent);

		return ent;
	}

	/**
	 * Create reference targets for entities with the same keys
	 *
	 * @param jCas
	 *            UIMA JCas Object
	 * @param entities
	 *            A collection of lists of entities to coreference
	 */
	protected void createReferenceTargets(final TextBlock block, final Collection<List<BaleenAnnotation>> entities) {
	    final int begin = block.toDocumentOffset(0);
	    final int end = block.toDocumentOffset(block.getCoveredText().length());

		for (final List<BaleenAnnotation> group : entities) {
			if (group.size() <= 1) {
				continue;
			}

			final ReferenceTarget rt = new ReferenceTarget(block.getJCas());
			rt.setBegin(begin);
			rt.setEnd(end);
			addToJCasIndex(rt);

			for (final BaleenAnnotation e : group) {
				if (e instanceof Entity) {
					((Entity) e).setReferent(rt);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setProperty(final BaleenAnnotation entity, final Method method, final Map<String, Object> additionalData) {
		if (method.getName().startsWith("set") && method.getName().substring(3, 4).matches("[A-Z]")
				&& method.getParameterCount() == 1) {
			String property = method.getName().substring(3);
			property = property.substring(0, 1).toLowerCase() + property.substring(1);
			final Object obj = additionalData.get(property);

			if (obj == null) {
				return;
			}

			if (method.getParameterTypes()[0].isAssignableFrom(obj.getClass())) {
				setPropertyObject(entity, method, obj);
			} else if (method.getParameterTypes()[0].isAssignableFrom(String.class)) {
				getMonitor().debug("Converting gazetteer object of type {} to String", obj.getClass().getName());
				setPropertyString(entity, method, obj.toString());
			} else if (List.class.isAssignableFrom(obj.getClass())
					&& CommonArrayFS.class.isAssignableFrom(method.getParameterTypes()[0])) {
				setPropertyArray(entity, method, (List<Object>) obj);
			}
		}
	}

	private void setPropertyObject(final BaleenAnnotation entity, final Method method, final Object obj) {
		try {
			method.invoke(entity, obj);
		} catch (final Exception e) {
			getMonitor().error(ERROR_CANT_ASSIGN_ENTITY_PROPERTY, e);
		}
	}

	private void setPropertyString(final BaleenAnnotation entity, final Method method, final String string) {
		try {
			method.invoke(entity, string);
		} catch (final Exception e) {
			getMonitor().error(ERROR_CANT_ASSIGN_ENTITY_PROPERTY, e);
		}
	}

	private void setPropertyArray(final BaleenAnnotation entity, final Method method, final List<Object> obj) {
		if (StringArray.class.isAssignableFrom(method.getParameterTypes()[0])) {
			try {
				final StringArray sa = listToStringArray(entity.getCAS().getJCas(), obj);
				method.invoke(entity, sa);
			} catch (final Exception e) {
				getMonitor().error(ERROR_CANT_ASSIGN_ENTITY_PROPERTY, e);
			}
		} else {
			getMonitor().error("Unsupported array type {} - property will be skipped",
					method.getParameterTypes()[0].getName());
		}
	}

	/**
	 * Replace repeated horizontal whitespace characters with a single space character, and return a
	 * TransformedString that maps between the original and normalised string
	 *
	 * @param s
	 *            The string to normalise
	 * @return A TransformedString mapping between the original and normalised text
	 */
	public static TransformedString normaliseString(final String s) {
		String remaining = s;
		final StringBuilder builder = new StringBuilder();

		String previousChar = "";
		final Map<Integer, Integer> indexMap = new HashMap<>();

		Integer index = 0;

		while (!remaining.isEmpty()) {
			indexMap.put(builder.length(), index);
			index++;

			String character = remaining.substring(0, 1);
			remaining = remaining.substring(1);

			if (!(character.matches("\\h") && previousChar.matches("\\h"))) {
				if (character.matches("\\h")) {
					character = " ";
				}

				builder.append(character);
			}

			previousChar = character;
		}
		indexMap.put(builder.length(), index);

		return new TransformedString(s, builder.toString(), indexMap);
	}

	private StringArray listToStringArray(final JCas jCas, final List<Object> l) {
		final StringArray sa = new StringArray(jCas, l.size());

		int index = 0;
		for (final Object o : l) {
			sa.set(index, o.toString());
			index++;
		}

		return sa;
	}

	@Override
	public void doDestroy() {
		gazetteer.destroy();
		gazetteer = null;

		entityType = null;
		trie = null;
	}
}
