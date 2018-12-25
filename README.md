# Notice
Notice is a Java library for sending push notification request to APNs. 

Notice adheres to Apple's HTTP/2-based APNs protocol and supports both TLS and token-based autentication.

**Note:** Install required dependencies when using Notice. 

## Getting Notice
You can download Notice as a .jar file and add it you your project directly. Make sure you have Notice's runtime dependencies on your classpath. They are:
  - [GSON 2.8.2](https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.2/)
  - [log4j 1.2.9](https://archive.apache.org/dist/logging/log4j/1.2.9/)

Notice requires Java 11 to build and run since it does not depend on netty, jetty, etc.

## Usage
Notice sends notifications asynchronously. You can customize it to send synchronously if necessary.
#### Setup Notification
Please check class PayloadBuilder() for more information.
```
// Basic setup for testing
PayloadBuilder payload = new PayloadBuilder()
				.setAlertBody("Testing...", false)
				.setBadge(1)
				.setSound("default");

/* payload with custom data */
// payload.addCutomData("number", 8).addCutomData("boolean", true);
// payload.setAlertBody("test alert body", false).build();

/* payload with custom data */
// payload.setAlertBody("AlertBody", true).setAlertLocArgs(new
// String[]{"james","chris"}).build();
```

#### TLS authentication
Notice currently support .p12 file only for tls authentication. To create a service,
```
  String file = "...directory path.../aps.p12";
  ApnsService service = new ApnsServiceBuilder()
      .setP12(file, "PASSWORD")
      .setDefaultExecutors();
```

#### Token authentication
To send push notifications via token authentication, get signing key from Apple.
If you type "get apns signing key apple" on google, it will guide you through the process of getting one.
```
  String file = "...directory path.../aps.p8";
  ApnsService service = new ApnsServiceBuilder()
      .authenticateWithToken("TEAMID0000", "KEYID00000", file)
      .setDefaultExecutors();
  // Default token time is 30 * 60 * 1000, which is 30 minutes
  // You can customize token time by calling .setTokenLifeTime(long tokenLifeTime)
  // More description about customization is explictly explained below
```

#### Custom Setting for Service Builder
setDefaultExecutors() have default settings for categories
  - poolSize: Default connection pool size is set to 3
  - timeUnit: Default TimeUnit for request timeout is in seconds (TimeUnit.SECONDS)
  - waitTime: Default time for request wait time is set to 5
	- isDevelopment: Default value is true. Set it to false for production
  - isSynchronous: Default value is false. 
  - timeOut: Default time for request time out is set to 15 (Duration.ofSeconds(15)
  - tokenLifetime: Default time for token life time is 30 minutes (30 * 60 * 1000);
  
You can even customize the executor for your own taste using createCustomExecutors().
```
  ApnsService service = new ApnsServiceBuilder()
      .setP12(file, "PASSWORD")
      .setDefaultExecutors();
      
  /* You can customize setting variables */ 
  // setPoolSize(int poolSize)
  // setTimeUnit(TimeUnit timeUnit)
  // setWaitTime(byte waitTime)
  // setDevelopment(boolean isDevelopment)
  // sendSynchronously() 
  // setRequestTimeOut(Duration duration)
  // setTokenLifeTime(long tokenLifeTime)
  
  /* Below is advanced setting */
  // @param coreSize: number of threads to start with
	// @param maxSize: maximum number of threads
	// @param aliveTime: idle time
  // createCustomExecutors(
			int coreSize, int maxSize, long aliveTime, TimeUnit unit)
```

#### Sending push notifications
Sending push notification using default settings
```
    String deviceToken = "SOME DEVICE TOKEN"; // This can be acquired from appDelegate on Xcode
	  Notification notification = new Notification(payload.build())
				.setUuid()
				.setExpiration(10)
				.setToken(deviceToken)
				.setTopic("com.example.yourApp");
		service.sendNotification(notification);
```
