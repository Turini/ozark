/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.oracle.ozark.core;

import com.oracle.ozark.event.ControllerMatched;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.mvc.Controller;
import javax.mvc.View;
import javax.mvc.Viewable;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Class ViewResponseFilter.
 *
 * @author Santiago Pericas-Geertsen
 */
@Controller
public class ViewResponseFilter implements ContainerResponseFilter {

    @Context
    private UriInfo uriInfo;

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    private Event<ControllerMatched> matchedEvent;

    @Inject
    private ControllerMatched matched;

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        // Fire ControllerMatched event
        matched.setUriInfo(uriInfo);
        matched.setResourceInfo(resourceInfo);
        matchedEvent.fire(matched);

        // Extract view information from @View if present
        final Method method = resourceInfo.getResourceMethod();
        if (method.getReturnType() == Void.TYPE) {
            View view = method.getAnnotation(View.class);
            if (view == null) {
                view = resourceInfo.getResourceClass().getAnnotation(View.class);
            }
            if (view != null) {
                final Viewable viewable = new Viewable(view.value());
                // Determine media type by inspecting @Produces
                Produces an = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
                if (an == null) {
                    an = resourceInfo.getResourceMethod().getClass().getAnnotation(Produces.class);
                }
                if (an != null) {
                    final String[] types = an.value();
                    if (types.length != 1) {
                        throw new ServerErrorException("Unable to determine response media type for "
                            + resourceInfo.getResourceMethod(), Response.Status.INTERNAL_SERVER_ERROR);
                    }
                    responseContext.setEntity(viewable, null, MediaType.valueOf(types[0]));
                } else {
                    responseContext.setEntity(viewable, null, MediaType.TEXT_HTML_TYPE);    // default
                }
                responseContext.setStatusInfo(OK);      // Needed for method returning void
            } else {
                throw new ServerErrorException("Controller method must specify view using @View annotation",
                        Response.Status.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
