package com.test.activemq.auth;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigUtils {

	private static String property = System.getProperty("user.dir");
	private static Properties pps;
	private static Log log = LogFactory.getLog(AuthBroker.class);
	static {
		String configPath = property + "/../conf/custom-auth.properties";
		log.info("config path is " + configPath);
		// configPath =
		// "/home/chenwenhao/workspace-mars/activemq-auth/resources/custom-auth.properties";
		pps = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(configPath));
			pps.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getConfig(String key) {
		String value = pps.getProperty(key);
		// System.out.println("password" + " = " + value);
		return value;
	}

	public static void main(String[] args) {
		ConfigUtils.getConfig("password");
	}

}
