package uk.gov.dstl.baleen.contentextractors.processor;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import uk.gov.dstl.baleen.types.structure.Caption;
import uk.gov.dstl.baleen.types.structure.Footer;
import uk.gov.dstl.baleen.types.structure.Header;
import uk.gov.dstl.baleen.types.structure.Link;
import uk.gov.dstl.baleen.types.structure.Paragraph;
import uk.gov.dstl.baleen.types.structure.Structure;
import uk.gov.dstl.baleen.types.structure.Style;
import uk.gov.dstl.baleen.uima.UimaMonitor;
import uk.gov.dstl.baleen.uima.UimaSupport;

public class RemoveEmptyText implements IContentProcessor {

	private List<Class<? extends Structure>> TEXT_TYPES = Arrays.asList(Paragraph.class, Caption.class, Header.class,
			Footer.class, Link.class, Style.class);

	private UimaContext context;

	private UimaSupport support;

	private UimaMonitor monitor;

	@Override
	public void initialize(UimaContext context, UimaSupport uimaSupport, UimaMonitor uimaMonitor)
			throws ResourceInitializationException {
		this.context = context;
		this.support = uimaSupport;
		this.monitor = uimaMonitor;
	}

	protected UimaMonitor getMonitor() {
		return monitor;
	}

	protected UimaSupport getSupport() {
		return support;
	}

	protected UimaContext getContext() {
		return context;
	}

	@Override
	public void process(JCas input, JCas ouput) throws IOException {
		getMonitor().info("Removing empty text structures");

		ouput.setDocumentText(input.getDocumentText());

		List<Structure> structures = new ArrayList<>(JCasUtil.select(input, Structure.class));
		for (Structure s : structures) {
			if (!(TEXT_TYPES.contains(s.getClass()) && s.getBegin() == s.getEnd())) {
				copy(ouput, s);
			}
		}

	}

	public void copy(JCas ouput, Structure s) {
		Constructor<? extends Structure> constructor;
		try {
			constructor = s.getClass().getConstructor(JCas.class, int.class, int.class);
			getSupport().add(constructor.newInstance(ouput, s.getBegin(), s.getEnd()));
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			getMonitor().warn("Can not copy Structure {}", s);
		}
	}

	@Override
	public void destroy() {

	}

}
