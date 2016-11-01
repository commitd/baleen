package uk.gov.dstl.baleen.collectionreaders.helpers;

import static org.junit.Assert.assertEquals;

import com.tenode.baleen.extraction.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;
import uk.gov.dstl.baleen.contentextractors.helpers.SimpleTagToStructureMapper;
import uk.gov.dstl.baleen.types.structure.Anchor;
import uk.gov.dstl.baleen.types.structure.Document;
import uk.gov.dstl.baleen.types.structure.Figure;
import uk.gov.dstl.baleen.types.structure.Heading;
import uk.gov.dstl.baleen.types.structure.Ordered;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Table;
import uk.gov.dstl.baleen.types.structure.TableBody;
import uk.gov.dstl.baleen.types.structure.TableCell;
import uk.gov.dstl.baleen.types.structure.TableHeader;
import uk.gov.dstl.baleen.types.structure.TableRow;

public class SimpleTagToStructureMapperTest {

	private Map<String, Class<? extends Structure>> typeToClassMapping = initMap();

	private SimpleTagToStructureMapper mapper;

	private JCas jcas;

	@Before
	public void setup() throws UIMAException {
		this.jcas = JCasFactory.createJCas();
		this.mapper = new SimpleTagToStructureMapper(jcas);
	}

	@Test
	public void testMapping() {
		for (Entry<String, Class<? extends Structure>> entry : typeToClassMapping.entrySet()) {
			Tag tag = new Tag(0, entry.getKey(),null, null);
			assertEquals(mapper.map(tag).get().getClass(), entry.getValue());
		}
	}

	private Map<String, Class<? extends Structure>> initMap() {
		Map<String, Class<? extends Structure>> map = new HashMap<>();
		map.put("h1", Heading.class);
		map.put("h2", Heading.class);
		map.put("h3", Heading.class);
		map.put("h4", Heading.class);
		map.put("h5", Heading.class);
		map.put("h6", Heading.class);
		map.put("p", Paragraph.class);
		map.put("pre", Paragraph.class);
		map.put("blockquote", Paragraph.class);
		map.put("q", Paragraph.class);
		map.put("ul", Ordered.class);
		map.put("ol", Ordered.class);
		map.put("li", Paragraph.class);
		map.put("dl", Ordered.class);
		map.put("dt", Paragraph.class);
		map.put("dd", Paragraph.class);
		map.put("table", Table.class);
		map.put("thead", TableHeader.class);
		map.put("tbody", TableBody.class);
		map.put("tr", TableRow.class);
		map.put("th", TableCell.class);
		map.put("td", TableCell.class);
		map.put("address", Paragraph.class);
		map.put("a", Anchor.class);
		map.put("img", Figure.class);
		map.put("ins", Paragraph.class);
		map.put("del", Paragraph.class);
		map.put("body", Document.class);
		return map;
	}
}
