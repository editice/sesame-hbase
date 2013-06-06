/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Interceptor that inserts some commonly used values into the model.
 * 
 * The inserted values are: - path, equal to request.getContextPath() (e.g.
 * /context) - basePath, equal to the fully qualified context path (e.g.
 * http://www.example.com/context/) - currentYear, equal to the current year
 * 
 * @author Herko ter Horst
 */
public class MessageHandlerInterceptor implements HandlerInterceptor {

	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		// nop
	}

	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler, ModelAndView mav) {
		HttpSession session = request.getSession();

		if (session != null) {
			Message message = (Message) session
					.getAttribute(Message.ATTRIBUTE_KEY);
			if (message != null
					&& !mav.getModelMap().containsKey(Message.ATTRIBUTE_KEY)) {
				mav.addObject(Message.ATTRIBUTE_KEY, message);
			}

			boolean shouldRemove = true;
			if (mav.hasView() && mav.getView() instanceof RedirectView) {
				shouldRemove = false;
			}
			if (mav.getViewName() != null
					&& mav.getViewName().startsWith("redirect:")) {
				shouldRemove = false;
			}

			if (shouldRemove) {
				session.removeAttribute(Message.ATTRIBUTE_KEY);
			}
		}
	}

	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		return true;
	}

}
