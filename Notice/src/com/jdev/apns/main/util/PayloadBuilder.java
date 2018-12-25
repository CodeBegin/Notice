/*
 * Copyright (c) [2018] [Seung Hwan, Jin]

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */

package com.jdev.apns.main.util;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.*;
import com.jdev.apns.main.model.APS;
import com.jdev.apns.main.model.Alert;

/**
 * Method that build payload
 * 
 * @author seunghwanjin
 */
public class PayloadBuilder {
	/**
	 * Name of default alert sound
	 * 
	 * For details about providing sound files for notifications,
	 * @see Preparing Custom Alert Sounds
	 */
	public static final String DEFAULT_SOUND_FILENAME = "default";
	
	/**
	 * Maximum payload size for remote notification 
	 */
	private static final int REMOTE_MAXIMUM_PAYLOAD_SIZE = 4096;
	private Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().
			setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
	
	private Integer badge = null;
	private String soundFileName = null;
	private Integer contentAvailable = null;
	private String category = null;
	private String threadId = null;
	
	private Boolean hasMoreAlertKeys = null; 
	private String alertTitle = null;
	private String alertBody = null;
	private String alertTitleLocKey = null;
	private String[] alertTitleLocArgs = null;
	private String alertLocKey = null;
	private String[] alertLocArgs = null;
	private String alertLaunchImage = null;
	
	private final HashMap<String, Object> customerData = new HashMap<>(); 
	
	public PayloadBuilder setBadge(Integer badge) {
		this.badge = badge;
		return this;
	}

	public PayloadBuilder setSound(String fileName) {
		soundFileName = fileName;
		return this;
	}

	public PayloadBuilder setContentAvailable(Integer contentAvailable) {
		this.contentAvailable = contentAvailable;
		return this;
	}

	public PayloadBuilder setCategory(String category) {
		this.category = category;
		return this;
	}

	public PayloadBuilder setThreadId(String threadId) {
		this.threadId = threadId;
		return this;
	}

	public PayloadBuilder setAlertTitle(String alertTitle) {
		this.alertTitle = alertTitle;
		return this;
	}

	/**
	 * This method sets alert body, which is often a message
	 * set hasMoreAlertKeys false if there is only an alert message
	 * 
	 * @param alertBody
	 * @param hasMoreAlertKeys
	 */
	public PayloadBuilder setAlertBody(String alertBody, boolean hasMoreAlertKeys) {
		this.alertBody = alertBody;
		this.hasMoreAlertKeys = hasMoreAlertKeys;
		return this;
	}
	public PayloadBuilder setAlertTitleLocKey(String alertTitleLocKey) {
		this.alertTitleLocKey = alertTitleLocKey;
		return this;
	}

	public PayloadBuilder setAlertTitleLocArgs(String[] alertTitleLocArgs) {
		this.alertTitleLocArgs = alertTitleLocArgs;
		return this;
	}

	public PayloadBuilder setAlertLocKey(String alertLocKey) {
		this.alertLocKey = alertLocKey;
		return this;
	}

	public PayloadBuilder setAlertLocArgs(String[] alertLocArgs) {
		this.alertLocArgs = alertLocArgs;
		return this;
	}

	public PayloadBuilder setAlertLaunchImage(String alertLaunchImage) {
		this.alertLaunchImage = alertLaunchImage;
		return this;
	}
	
	/**
	 * Add a custom data to payload
	 * 
	 * @param key the key of the custom data; it should be String
	 * @param value
	 *  
	 * @return a reference to Payload
	 * @throws IllegalArgumentException when payload size exceeds the maximum
	 */
	public PayloadBuilder addCutomData(final String key, final Object value) {
		customerData.put(key, value);
		return this;
	}

	public String build() {
		final JsonObject payload = new JsonObject();
		
		final JsonObject aps = new JsonObject();
		if (badge != null) {
			aps.addProperty(APS.BADGE_KEY.key(), badge);
		}
		
		if (soundFileName != null) {
			aps.addProperty(APS.SOUND_KEY.key(), soundFileName);
		}
		
		if (contentAvailable != null) {
			aps.addProperty(APS.CONTENT_AVAILABLE_KEY.key(), contentAvailable);
		}
		
		if (category != null) {
			aps.addProperty(APS.CATEGORY_KEY.key(), category);
		}
		
		if (threadId != null) {
			aps.addProperty(APS.THREAD_ID_KEY.key(), threadId);
		}
		
		if (hasMoreAlertKeys != null) {
			if (!hasMoreAlertKeys) {
				aps.addProperty(APS.ALERT_KEY.key(), alertBody);
			} else {
				JsonObject alertObject = new JsonObject();

				if (alertTitle != null) {
					alertObject.addProperty(Alert.ALERT_TITLE_KEY.key(), alertTitle);
				}

				if (alertBody != null) {
					alertObject.addProperty(Alert.ALERT_BODY_KEY.key(), alertBody);
				}

				if (alertTitleLocKey != null) {
					alertObject.addProperty(Alert.ALERT_TITLE_LOC_KEY.key(), alertTitleLocKey);
				}

				if (alertTitleLocArgs != null) {
					JsonArray titleLocArgs = new JsonArray();
					for (String arg:alertTitleLocArgs) {
						titleLocArgs.add(arg);
					}
					alertObject.add(Alert.ALERT_TITLE_LOC_ARGS.key(),titleLocArgs);
				}

				if (alertLocKey != null) {
					alertObject.addProperty(Alert.ALERT_LOC_KEY.key(), alertLocKey);
				}

				if (alertLocArgs != null) {
					JsonArray locArgs = new JsonArray();
					for (String arg:alertLocArgs) {
						locArgs.add(arg);
					}
					alertObject.add(Alert.ALERT_LOC_ARGS.key(),locArgs);
				}

				if (alertLaunchImage != null) {
					alertObject.addProperty(Alert.ALERT_LOC_KEY.key(), alertLaunchImage);
				}
			}
		}//initiated only when one of alert keys is set
		payload.add(APS.APS_KEY.key(), aps);		
		
		for (final Map.Entry<String, Object> entry:customerData.entrySet()) {
			Object value = entry.getValue();
			payload.add(entry.getKey(), gson.toJsonTree(value, value.getClass()));
		}
		
		String payloadString = gson.toJson(payload);  
		if (payloadString.getBytes(StandardCharsets.UTF_8).length > REMOTE_MAXIMUM_PAYLOAD_SIZE) {
			throw new IllegalArgumentException("Payload exceeded maximum size");
		}
		
		return payloadString;
	}
	
	@Override
	public String toString() {
		return build().toString();
	}
	
}
