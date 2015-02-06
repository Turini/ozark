package com.oracle.ozark.test.thymeleaf;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.mvc.engine.ViewEngine;
import javax.mvc.engine.ViewEngineContext;
import javax.mvc.engine.ViewEngineException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Class ThymeleafViewEngine.
 *
 * @author Rodrigo Turini
 */
@ApplicationScoped
public class ThymeleafViewEngine implements ViewEngine {

	private static final String VIEW_BASE = "/WEB-INF/";

	@Inject
	private ServletContext servletContext;

	private TemplateResolver resolver;


	public ThymeleafViewEngine() {
		resolver = new ServletContextTemplateResolver();
		resolver.setPrefix(VIEW_BASE);
	}

	@Override
	public boolean supports(String view) {
		return view.endsWith("html");
	}

	@Override
	public void processView(ViewEngineContext context) throws ViewEngineException {
		try {
			TemplateEngine engine = new TemplateEngine();
			engine.setTemplateResolver(resolver);

			HttpServletRequest request = context.getRequest();
			HttpServletResponse response = context.getResponse();
			
			WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			
			ctx.setVariables(context.getModels());
			
			engine.process(context.getView(), ctx, response.getWriter());
			
		} catch (IOException e) {
			throw new ViewEngineException(e);
		}
	}
}