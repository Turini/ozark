package com.oracle.ozark.test.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mvc.engine.ViewEngine;
import javax.mvc.engine.ViewEngineContext;
import javax.mvc.engine.ViewEngineException;
import javax.servlet.ServletContext;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 * Class ThymeleafViewEngine.
 *
 * @author Rodrigo Turini
 */
@ApplicationScoped
public class VelocityViewEngine implements ViewEngine {

	private static final String VIEW_BASE = "/WEB-INF/";

	@Inject
	private ServletContext servletContext;

	private VelocityEngine velocityEngine;

	@PostConstruct
	public void init()  {
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty("resource.loader", "webapp");
		velocityEngine.setProperty("webapp.resource.loader.class", "org.apache.velocity.tools.view.servlet.WebappLoader");
		velocityEngine.setProperty("webapp.resource.loader.path", VIEW_BASE);
		velocityEngine.setApplicationAttribute("javax.servlet.ServletContext", servletContext);
		velocityEngine.init();
	}

	@Override
	public boolean supports(String view) {
		return view.endsWith("vm");
	}

	@Override
	public void processView(ViewEngineContext context) throws ViewEngineException {
		try {
			Template template = velocityEngine.getTemplate(context.getView());
			VelocityContext velocityContext = new VelocityContext(context.getModels());
			template.merge(velocityContext, context.getResponse().getWriter());
		} catch (IOException e) {
			throw new ViewEngineException(e);
		}
	}
}