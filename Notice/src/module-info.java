/**
 * 
 */
/**
 * @author seunghwanjin
 *
 */
module Notice {
	  requires gson;
	  requires transitive log4j;
	  requires transitive java.net.http;
	  requires transitive java.sql;
	  requires java.base;

	  exports com.jdev.apns.test;
	  exports com.jdev.apns.main.impl;
	  exports com.jdev.apns.main;
	  exports com.jdev.apns.main.model;
	  exports com.jdev.apns.main.util;
}