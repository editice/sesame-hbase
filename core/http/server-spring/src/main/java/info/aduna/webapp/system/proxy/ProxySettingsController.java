/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.system.proxy;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import info.aduna.app.net.ProxySettings;
import info.aduna.webapp.util.HttpServerUtil;

/**
 * 
 * @author Herko ter Horst
 */
public class ProxySettingsController {

	// FIXME: fix this non-implementation
	private ProxySettings PROXY_SETTINGS = null;

	private void setProxies(Map<String, Object> params,
			HttpServletResponse response) throws IOException {
		boolean useProxies = HttpServerUtil.isTrue(HttpServerUtil
				.getPostDataParameter(params, "connection"));

		if (!useProxies) {
			PROXY_SETTINGS.setProxiesEnabled(false);
		} else {
			String httpProxyHost = HttpServerUtil.getPostDataParameter(params,
					"httpProxyHost");
			String httpProxyPort = HttpServerUtil.getPostDataParameter(params,
					"httpProxyPort");
			if (!HttpServerUtil.isEmpty(httpProxyHost)) {
				PROXY_SETTINGS.setHttpProxyHost(httpProxyHost);
				if (checkPort(httpProxyPort)) {
					PROXY_SETTINGS.setHttpProxyPort(httpProxyPort);
				}
			}

			String httpsProxyHost = HttpServerUtil.getPostDataParameter(params,
					"httpsProxyHost");
			String httpsProxyPort = HttpServerUtil.getPostDataParameter(params,
					"httpsProxyPort");
			if (!HttpServerUtil.isEmpty(httpsProxyHost)) {
				PROXY_SETTINGS.setHttpsProxyHost(httpsProxyHost);
				if (checkPort(httpsProxyPort)) {
					PROXY_SETTINGS.setHttpsProxyPort(httpsProxyPort);
				}
			}

			String ftpProxyHost = HttpServerUtil.getPostDataParameter(params,
					"ftpProxyHost");
			String ftpProxyPort = HttpServerUtil.getPostDataParameter(params,
					"ftpProxyPort");
			if (!HttpServerUtil.isEmpty(ftpProxyHost)) {
				PROXY_SETTINGS.setFtpProxyHost(ftpProxyHost);
				if (checkPort(ftpProxyPort)) {
					PROXY_SETTINGS.setFtpProxyPort(ftpProxyPort);
				}
			}

			String socksProxyHost = HttpServerUtil.getPostDataParameter(params,
					"socksProxyHost");
			String socksProxyPort = HttpServerUtil.getPostDataParameter(params,
					"socksProxyPort");
			if (!HttpServerUtil.isEmpty(socksProxyHost)) {
				PROXY_SETTINGS.setSocksProxyHost(socksProxyHost);
				if (checkPort(socksProxyPort)) {
					PROXY_SETTINGS.setHttpProxyPort(socksProxyPort);
				}
			}

			String proxyExceptions = HttpServerUtil.getPostDataParameter(
					params, "proxyExceptions");
			if (!HttpServerUtil.isEmpty(proxyExceptions)) {
				PROXY_SETTINGS.setNonProxyHostsStarting(proxyExceptions);
			}

			PROXY_SETTINGS.setProxiesEnabled(true);
		}

		PROXY_SETTINGS.save();
	}

	private boolean checkPort(String proxyPort) throws IOException {
		boolean result = false;

		int port = -1;
		if (!HttpServerUtil.isEmpty(proxyPort)) {
			try {
				port = Integer.parseInt(proxyPort);
				if (port > 0 || port < 65536) {
					result = true;
				}
			} catch (NumberFormatException nfe) {
				result = false;
			}
		}

		return result;
	}

}
