package uk.gov.dstl.baleen.annotators.misc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;

import uk.gov.dstl.baleen.types.semantic.Entity;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;
import uk.gov.dstl.baleen.uima.BaleenTextAwareAnnotator;
import uk.gov.dstl.baleen.uima.ComparableEntitySpan;
import uk.gov.dstl.baleen.uima.data.TextBlock;
import uk.gov.dstl.baleen.uima.utils.ComparableEntitySpanUtils;

/**
 * Creates entity annotations for each piece of text that is the same as the covered text.
 * <p>
 * This is useful when a model is used (rather than a regex) and it only finds a subset of the
 * mentions in a document.
 * <p>
 * If an annotation of the same type already exists on the covering text then another is not added.
 *
 * @baleen.javadoc
 */
public class MentionedAgain extends BaleenTextAwareAnnotator {

	@Override
    protected void doProcessTextBlock(final TextBlock block) throws AnalysisEngineProcessException {

    // WE look through the JCas for the entities, but we only look for matches in this block
    final String text = block.getCoveredText();

    final Collection<Entity> list = JCasUtil.select(block.getJCas(), Entity.class);

		final Set<ComparableEntitySpan> spans = new HashSet<>(list.size());

		list.stream()
				.forEach(e -> {
					final Pattern pattern = Pattern.compile("\\b" + Pattern.quote(e.getCoveredText()) + "\\b");
					final Matcher matcher = pattern.matcher(text);
					while (matcher.find()) {
						if (!ComparableEntitySpanUtils.existingEntity(list, matcher.start(), matcher.end(), e.getClass())) {
                          spans.add(new ComparableEntitySpan(e, block.toDocumentOffset(matcher.start()),
                             block.toDocumentOffset(matcher.end())));
						}
					}
				});

		spans.stream().forEach(s -> {
            final Entity newEntity = ComparableEntitySpanUtils.copyEntity(block.getJCas(), s.getBegin(), s.getEnd(), s.getEntity());

			if (s.getEntity().getReferent() == null) {
				// Make them the same
                final ReferenceTarget rt = new ReferenceTarget(block.getJCas());
				addToJCasIndex(rt);

				s.getEntity().setReferent(rt);
			}

			newEntity.setReferent(s.getEntity().getReferent());

			addToJCasIndex(newEntity);
		});
	}

}
