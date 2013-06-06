/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.system;

import info.aduna.app.AppConfiguration;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class SystemOverviewController implements Controller {

	private String view;

	private AppConfiguration config;

	private ServerInfo server;

	public SystemOverviewController() {
		server = new ServerInfo();
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		ModelAndView result = new ModelAndView();
		result.setViewName(view);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("appConfig", config);
		model.put("server", server);
		model.put("memory", new MemoryInfo());
		result.addAllObjects(model);

		return result;
	}

	public AppConfiguration getConfig() {
		return config;
	}

	public void setConfig(AppConfiguration config) {
		this.config = config;
	}

	public static class ServerInfo {
		private String os;

		private String java;

		private String user;

		public ServerInfo() {
			os = System.getProperty("os.name") + " "
					+ System.getProperty("os.version") + " ("
					+ System.getProperty("os.arch") + ")";
			java = System.getProperty("java.vendor") + " "
					+ System.getProperty("java.vm.name") + " "
					+ System.getProperty("java.version");
			user = System.getProperty("user.name");
		}

		public String getOs() {
			return os;
		}

		public String getJava() {
			return java;
		}

		public String getUser() {
			return user;
		}
	}

	public static class MemoryInfo {

		private int maximum;
		private int used;
		private float percentageInUse;

		public MemoryInfo() {
			Runtime runtime = Runtime.getRuntime();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();
			long maxMemory = runtime.maxMemory();

			// Memory usage (percentage)
			percentageInUse = (float) ((float) usedMemory / (float) maxMemory);

			// Memory usage in MB
			used = (int) (usedMemory / 1024 / 1024);
			maximum = (int) (maxMemory / 1024 / 1024);
		}

		public int getMaximum() {
			return maximum;
		}

		public int getUsed() {
			return used;
		}

		public float getPercentageInUse() {
			return percentageInUse;
		}
	}
}
