package uk.gov.dstl.baleen.contentextractors.helpers;

import java.util.Objects;
import java.util.Optional;

import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.uima.jcas.JCas;
import org.xml.sax.Attributes;

import com.tenode.baleen.extraction.Tag;

import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.ListItem;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Section;
import uk.gov.dstl.baleen.types.structure.Sheet;
import uk.gov.dstl.baleen.types.structure.Slide;
import uk.gov.dstl.baleen.types.structure.SlideShow;
import uk.gov.dstl.baleen.types.structure.SpreadSheet;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;
import uk.gov.dstl.baleen.types.structure.Unordered;

public class SimpleTagToStructureMapper implements TagToStructureMapper {

	private enum DocumentType {
		SPREADSHEET, DOCUMENT, SLIDESHOW, UNKNOWN
	}

	private static final String URI = "http://www.w3.org/1999/xhtml";

	private final JCas jcas;

	private final DocumentType documentType;

	public SimpleTagToStructureMapper(JCas jcas, Metadata metadata) {
		this.jcas = jcas;
		documentType = getDocumentType(metadata.get(HttpHeaders.CONTENT_TYPE));
	}

	private DocumentType getDocumentType(String type) {
		switch (type == null ? "" : type) {
		case "application/vnd.ms-excel":
			// fall through
		case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
			// fall through
		case "application/vnd.openxmlformats-officedocument.spreadsheetml.template":
			// fall through
		case "application/vnd.ms-excel.sheet.macroEnabled.12":
			// fall through
		case "application/vnd.ms-excel.template.macroEnabled.12":
			// fall through
		case "application/vnd.ms-excel.addin.macroEnabled.12":
			// fall through
		case "application/vnd.ms-excel.sheet.binary.macroEnabled.12":
			// fall through
		case "text/csv":
			return DocumentType.SPREADSHEET;
		case "application/msword":
			// fall through
		case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
			// fall through
		case "application/vnd.openxmlformats-officedocument.wordprocessingml.template":
			// fall through
		case "application/vnd.ms-word.document.macroEnabled.12":
			// fall through
		case "application/vnd.ms-word.template.macroEnabled.12":
			return DocumentType.DOCUMENT;
		case "application/vnd.ms-powerpoint":
			// fall through
		case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
			// fall through
		case "application/vnd.openxmlformats-officedocument.presentationml.template":
			// fall through
		case "application/vnd.openxmlformats-officedocument.presentationml.slideshow":
			// fall through
		case "application/vnd.ms-powerpoint.addin.macroEnabled.12":
			// fall through
		case "application/vnd.ms-powerpoint.presentation.macroEnabled.12":
			// fall through
		case "application/vnd.ms-powerpoint.template.macroEnabled.12":
			// fall through
		case "application/vnd.ms-powerpoint.slideshow.macroEnabled.12":
			return DocumentType.SLIDESHOW;
		default:
			return DocumentType.UNKNOWN;
		}
	}

	@Override
	public Optional<Structure> map(final Tag tag) {
		final Structure structure = mapInternal(tag);
		if (structure != null) {
			final String classValue = tag.getAttributes().getValue("class");
			structure.setElementClass(classValue);
			structure.setDepth(tag.getDepth());
		}
		return Optional.ofNullable(structure);
	}

	private Structure mapInternal(Tag tag) {

		switch (tag.getType()) {
		case "h1":
			Heading h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(1);
			return h;
		case "h2":
			h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(2);
			return h;
		case "h3":
			h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(3);
			return h;
		case "h4":
			h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(4);
			return h;
		case "h5":
			h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(5);
			return h;
		case "h6":
			h = new Heading(jcas, tag.getStart(), tag.getEnd());
			h.setLevel(6);
			return h;

		case "ul":
			// fall through
		case "dl":
			return new Unordered(jcas, tag.getStart(), tag.getEnd());

		case "ol":
			return new Ordered(jcas, tag.getStart(), tag.getEnd());

		case "table":
			return new Table(jcas, tag.getStart(), tag.getEnd());

		case "thead":
			return new TableHeader(jcas, tag.getStart(), tag.getEnd());

		case "tbody":
			return new TableBody(jcas, tag.getStart(), tag.getEnd());

		case "tr":
			return new TableRow(jcas, tag.getStart(), tag.getEnd());

		case "th":
			// fall through
		case "td":
			return new TableCell(jcas, tag.getStart(), tag.getEnd());

		case "a":
			return new Anchor(jcas, tag.getStart(), tag.getEnd());

		case "img":
			return new Figure(jcas, tag.getStart(), tag.getEnd());

		case "li":
			return new ListItem(jcas, tag.getStart(), tag.getEnd());

		case "p":
			final String styleClazz = tag.getAttributes().getValue("class");
			if (styleClazz == null) {
				return new Paragraph(jcas, tag.getStart(), tag.getEnd());
			}

			// Tika Word maps everything is a paragraph...
			switch (styleClazz.toLowerCase()) {
			case "list_paragraph":
				return new ListItem(jcas, tag.getStart(), tag.getEnd());
			case "header":
				return new Header(jcas, tag.getStart(), tag.getEnd());
			case "footer":
				return new Footer(jcas, tag.getStart(), tag.getEnd());
			default:
				return new Paragraph(jcas, tag.getStart(), tag.getEnd());
			}

		case "pre":
			// fall through
		case "blockquote":
			// fall through
		case "q":
			// fall through
		case "dt":
			// fall through
		case "dd":
			// fall through
		case "address":
			// fall through
		case "ins":
			// fall through
		case "del":
			return new Paragraph(jcas, tag.getStart(), tag.getEnd());

		case "body":
			return processBody(tag);

		case "div":
			return processDiv(tag);

		case "u":
		case "i":
		case "b":
			// TODO
			return new Style(jcas, tag.getStart(), tag.getEnd());

		case "html":
		case "head":
		case "title":
		case "meta":
			// We deal with metadata through Tika API interface
			return null;

		case "br":
			// Not interested in these tags from structural perspective
			return null;

		default:
			System.err.println(tag.getType());

			return null;
		}

	}

	private Structure processBody(Tag tag) {
		switch (documentType) {
		case SPREADSHEET:
			return new SpreadSheet(jcas, tag.getStart(), tag.getEnd());
		case SLIDESHOW:
			return new SlideShow(jcas, tag.getStart(), tag.getEnd());
		case DOCUMENT:
			// fall through
		default:
			return new Document(jcas, tag.getStart(), tag.getEnd());
		}
	}

	private Structure processDiv(Tag tag) {
		final Attributes attributes = tag.getAttributes();
		final String divClass = attributes.getValue("class");

		if (Objects.equals(DocumentType.SLIDESHOW, documentType) && Objects.equals("slide-content", divClass)) {
			return new Slide(jcas, tag.getStart(), tag.getEnd());
		} else if (Objects.equals(DocumentType.SPREADSHEET, documentType) && Objects.equals("page", divClass)) {
			return new Sheet(jcas, tag.getStart(), tag.getEnd());
		} else if (Objects.equals("header", divClass)) {
			return new Header(jcas, tag.getStart(), tag.getEnd());
		} else if (Objects.equals("footer", divClass)) {
			return new Footer(jcas, tag.getStart(), tag.getEnd());
		} else {
			return new Section(jcas, tag.getStart(), tag.getEnd());
		}
	}

}
