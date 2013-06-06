/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.views;

import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

import info.aduna.io.IOUtil;

/**
 * 
 * @author Herko ter Horst
 */
public class SimpleCustomResponseView implements View {

	public static final String SC_KEY = "sc";

	public static final String CONTENT_KEY = "content";

	public static final String CONTENT_LENGTH_KEY = "contentLength";

	public static final String CONTENT_TYPE_KEY = "contentType";

	private static final int DEFAULT_SC = HttpServletResponse.SC_OK;

	private static final SimpleCustomResponseView INSTANCE = new SimpleCustomResponseView();

	public static SimpleCustomResponseView getInstance() {
		return INSTANCE;
	}

	public String getContentType() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		int sc = DEFAULT_SC;
		if (model.containsKey(SC_KEY)) {
			sc = (Integer) model.get(SC_KEY);
		}
		String contentType = (String) model.get(CONTENT_TYPE_KEY);
		Integer contentLength = (Integer) model.get(CONTENT_LENGTH_KEY);
		InputStream content = (InputStream) model.get(CONTENT_KEY);

		try {
			response.setStatus(sc);

			ServletOutputStream out = response.getOutputStream();
			if (content != null) {
				if (contentType != null) {
					response.setContentType(contentType);
				}
				if (contentLength != null) {
					response.setContentLength(contentLength);
				}
				IOUtil.transfer(content, out);
			} else {
				response.setContentLength(0);
			}
			out.close();
		} finally {
			if (content != null) {
				content.close();
			}
		}
	}
}
