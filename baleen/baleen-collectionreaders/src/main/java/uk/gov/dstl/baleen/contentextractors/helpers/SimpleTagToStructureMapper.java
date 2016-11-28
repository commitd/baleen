package uk.gov.dstl.baleen.contentextractors.helpers;

import java.util.Objects;
import java.util.Optional;

import org.apache.uima.jcas.JCas;
import org.xml.sax.Attributes;
import com.tenode.baleen.extraction.Tag;

import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;
import uk.gov.dstl.baleen.types.structure.Unordered;

public class SimpleTagToStructureMapper implements TagToStructureMapper {

	private static final String URI = "http://www.w3.org/1999/xhtml";

	private final JCas jcas;

	public SimpleTagToStructureMapper(JCas jcas) {
		this.jcas = jcas;
	}

	@Override
	public Optional<Structure> map(final Tag tag) {
		Structure structure = mapInternal(tag);
		if (structure != null) {
			structure.setDepth(tag.getDepth());
		}
		return Optional.ofNullable(structure);
	}

	private Structure mapInternal(Tag tag) {
		switch (tag.getType()) {
		case "h1": {
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(1);
			return h;
		}
		case "h2": {
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(2);
			return h;
		}
		case "h3": {
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(3);
			return h;
		}
		case "h4": {
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(4);
			return h;
		}
		case "h5": {
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(5);
			return h;
		}
		case "h6": {
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(6);
			return h;
		}
		case "p": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "pre": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "blockquote": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "q": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "ul": {
			return new Unordered(jcas, tag.getStart(), tag.getEnd());
		}
		case "ol": {
			return new Ordered(jcas, tag.getStart(), tag.getEnd());
		}
		case "li": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "dl": {
			return new Unordered(jcas, tag.getStart(), tag.getEnd());
		}
		case "dt": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "dd": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "table": {
			return new Table(jcas, tag.getStart(), tag.getEnd());
		}
		case "thead": {
			return new TableHeader(jcas, tag.getStart(), tag.getEnd());
		}
		case "tbody": {
			return new TableBody(jcas, tag.getStart(), tag.getEnd());
		}
		case "tr": {
			return new TableRow(jcas, tag.getStart(), tag.getEnd());
		}
		case "th": {
			return new TableCell(jcas, tag.getStart(), tag.getEnd());
		}
		case "td": {
			return new TableCell(jcas, tag.getStart(), tag.getEnd());
		}
		case "address": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "a": {
			return new Anchor(jcas, tag.getStart(), tag.getEnd());
		}
		case "img": {
			return new Figure(jcas, tag.getStart(), tag.getEnd());
		}
		case "ins": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "del": {
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());
		}
		case "body": {
			return new Document(jcas, tag.getStart(), tag.getEnd());
		}
		case "div": {
			return processDiv(tag);
		}
		default: {
			return null;
		}
		}
	}

	private Structure processDiv(Tag tag) {
		Attributes attributes = tag.getAttributes();
		String divClass = attributes.getValue(URI, "class");
		if (Objects.equals("slide-content", divClass)) {
			return new Slide(jcas, tag.getStart(), tag.getEnd());
		}
		return new Section(jcas, tag.getStart(), tag.getEnd());
	}

}
