package uk.gov.dstl.baleen.contentextractors.processor.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCopier;

public abstract class AbstractRegionRemovingContentProcessor extends AbstractContentProcessor {

	public static final class Span {
		private final int begin;

		private final int end;

		public Span(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}

		public Span(Annotation a) {
			this(a.getBegin(), a.getEnd());
		}

		public int getBegin() {
			return begin;
		}

		public int getEnd() {
			return end;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + begin;
			result = prime * result + end;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Span other = (Span) obj;
			if (begin != other.begin) {
				return false;
			}
			if (end != other.end) {
				return false;
			}
			return true;
		}

		public boolean contains(Annotation a) {
			return begin <= a.getBegin() && a.getEnd() <= end;
		}

		public boolean overlapsOnOneSide(Annotation a) {
			return a.getBegin() < begin && begin <= a.getEnd() && a.getEnd() <= end
					|| begin <= a.getBegin() && a.getBegin() <= end && end < a.getEnd();
		}

		public void clip(Annotation a) {
			if (a.getBegin() < begin && begin <= a.getEnd() && a.getEnd() <= end) {
				a.setEnd(begin);
			}

			if (begin <= a.getBegin() && a.getBegin() <= end && end < a.getEnd()) {
				a.setBegin(end);
			}
		}

		public boolean touches(Span s) {
			return !(s.getEnd() < getBegin() || getEnd() < s.getBegin());
		}

	}

	@Override
	public JCas process(JCas input, JCas temporary) throws IOException {
		final Collection<Span> badSpans = findSpansToRemove(input);

		if (badSpans == null || badSpans.isEmpty()) {
			return input;
		}

		// The bad spans are likely to be a mess, so we simplify
		final List<Span> cover = calculateMinimalCover(badSpans);

		if (cover.size() == 1 && cover.get(0).getBegin() == 0
				&& cover.get(0).getEnd() == input.getDocumentText().length()) {
			// Everything is bad.... so return the empty JCas
			return temporary;
		}

		// Remove any annotation which are complete covered by bad span
		// and 'clip' partially overlapping spans to they don't start/end inside
		// the bad span
		removeAnnotatationUnderCover(input, cover);

		// Get the new text with bad spans removed
		final String text = retainTextOutsideCover(input.getDocumentText(), cover);

		// Now shift the begin/end offset of the spans to account for the
		// removed lumps of text
		adjustAnnotationByOffsets(input, cover);

		// Use the temporary jcas as our output, copying over the good
		// annotations.
		temporary.setDocumentText(text);
		CasCopier.copyCas(input.getCas(), temporary.getCas(), false);
		return temporary;
	}

	private void adjustAnnotationByOffsets(JCas input, List<Span> cover) {
		final Collection<Annotation> all = JCasUtil.select(input, Annotation.class);

		for (final Annotation a : all) {

			final int oldBegin = a.getBegin();
			final int oldEnd = a.getEnd();
			int newBegin = a.getBegin();
			int newEnd = a.getEnd();

			for (final Span s : cover) {
				final int length = s.getEnd() - s.getBegin();
				// NOTE: we can just look at end as we've clipped everything
				// else to beginning
				if (s.getEnd() <= oldBegin) {
					newBegin -= length;
				}

				if (s.getEnd() <= oldEnd) {
					newEnd -= length;
				}
			}

			a.setBegin(newBegin);
			a.setEnd(newEnd);
		}

	}

	private String retainTextOutsideCover(String documentText, List<Span> cover) {
		final StringBuilder sb = new StringBuilder();

		int offset = 0;
		for (final Span s : cover) {
			sb.append(documentText).substring(offset, s.getBegin());
			offset = s.getEnd();
		}

		sb.append(documentText).substring(offset);

		return sb.toString();
	}

	private void removeAnnotatationUnderCover(JCas input, List<Span> cover) {
		// Remove all annotations in input which are under the spans
		// Amend all spans which intersect in one end, nothing else we can do!
		final Set<Annotation> toRemove = new HashSet<>();
		final Collection<Annotation> all = JCasUtil.select(input, Annotation.class);
		for (final Span s : cover) {

			for (final Annotation a : all) {
				if (s.contains(a)) {
					toRemove.add(a);
				} else if (s.overlapsOnOneSide(a)) {
					s.clip(a);
				}
			}
		}

		getSupport().remove(toRemove);
	}

	private List<Span> calculateMinimalCover(Collection<Span> collection) {
		List<Span> list;
		if (collection instanceof List) {
			list = (List<Span>) collection;
		} else {
			list = new ArrayList<>(collection);
		}
		Collections.sort(list, (a, b) -> {
			int compare = Integer.compare(a.getBegin(), b.getBegin());
			if (compare == 0) {
				compare = Integer.compare(a.getEnd(), b.getEnd());
			}
			return compare;
		});

		// We now construct a new list so that
		final List<Span> cover = new LinkedList<>();

		Span currentCover = null;
		for (final Span s : list) {
			if (currentCover == null) {
				currentCover = s;
				continue;
			}

			if (currentCover.touches(s)) {
				// Current cover touches s, so we 'eat' s
				currentCover = new Span(Math.min(s.getBegin(), currentCover.getBegin()),
						Math.max(s.getEnd(), currentCover.getEnd()));
			} else {
				// Doesn't overlap...save the old and move to the new one
				cover.add(currentCover);
				currentCover = s;
			}
		}

		if (currentCover != null) {
			cover.add(currentCover);
		}

		return cover;
	}

	protected abstract Collection<Span> findSpansToRemove(JCas input);

	protected static Collection<Span> toSpan(Collection<? extends Annotation> annotations) {
		if (annotations == null || annotations.isEmpty()) {
			return Collections.emptySet();
		}

		return annotations.stream().map(Span::new).collect(Collectors.toList());
	}

	@SafeVarargs
	protected final Collection<Span> combineSpans(Collection<Span>... collections) {
		if (collections == null || collections.length == 0) {
			return Collections.emptySet();
		}

		final List<Span> combined = new LinkedList<>();
		for (final Collection<Span> c : collections) {
			if (c != null) {
				combined.addAll(c);
			}
		}

		return combined;
	}
}
