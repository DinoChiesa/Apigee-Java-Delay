# Sleeper callout

This directory contains the Java source code and pom.xml file
required to compile a simple custom policy for Apigee Edge. The
policy does one simple thing: sleeps for a designated or random amount of time.

## Why might this be useful?

1. Some people are concerned that an API that generates errors and always returns
within a fixed time may be subject to remote timing attacks. This callout would alleviate that concern.

2. In cases in which a caller is suspect, or has caused several consecutive errors,
this callout could force a delay before retry.

## Disclaimer

This example is not an official Google product, nor is it part of an
official Google product.


## Using this policy

You do not need to build the source code in order to use the policy
in Apigee Edge.  All you need is the built JAR, and the appropriate
configuration for the policy.  If you want to build it, feel free.
The instructions are at the bottom of this readme.


1. copy the jar file, available in  target/edge-custom-sleeper-1.0.1.jar , if you have built the jar, or in [the repo](bundle/apiproxy/resources/java/edge-custom-sleeper-1.0.1.jar) if you have not, to your apiproxy/resources/java directory. You can do this offline, or using the graphical Proxy Editor in the Apigee Edge Admin Portal.

2. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look
   like this:
   ```xml
    <JavaCallout name='Java-Sleep-1'>
        ...
      <ClassName>com.google.apigee.edgecallouts.sleeper.SleeperCallout</ClassName>
      <ResourceURL>java://edge-custom-sleeper-1.0.1.jar</ResourceURL>
    </JavaCallout>
   ```

3. use the Edge UI, or a command-line tool like [importAndDeploy.js](https://github.com/DinoChiesa/apigee-edge-js/blob/master/examples/importAndDeploy.js) or similar to
   import the proxy into an Edge organization, and then deploy the proxy .
   Eg,
   ```node ./importAndDeploy.js -v -o $ORG -e $ENV -d ./bundle```

4. Use a client to generate and send http requests to the proxy you just deployed . Eg,
   ```
   curl -i "https://$ORG-$ENV.apigee.net/sleeper/t1"
   ```


## Notes on Usage

There is one callout class, com.google.apigee.edgecallouts.sleeper.SleeperCallout.

The delay time in milliseconds for the policy is configured via a property in the XML. By default it is a random number between 850 and 1850 milliseconds.

## Example: Sleep a random amount of time

```xml
<JavaCallout name='Java-Sleep-1'>
  <ClassName>com.google.apigee.edgecallouts.sleeper.SleeperCallout</ClassName>
  <ResourceURL>java://edge-custom-sleeper-1.0.1.jar</ResourceURL>
</JavaCallout>
```

## Example: Sleep 4 seconds

```xml
<JavaCallout name='Java-Sleep-1'>
  <Properties>
    <!-- sleep 4 seconds -->
    <Property name='delay'>4000</Property>
  </Properties>
  <ClassName>com.google.apigee.edgecallouts.sleeper.SleeperCallout</ClassName>
  <ResourceURL>java://edge-custom-sleeper-1.0.1.jar</ResourceURL>
</JavaCallout>
```

## Example: Sleep a time determined by a variable

```xml
<JavaCallout name='Java-Sleep-1'>
  <Properties>
    <Property name='delay'>{delayTime}</Property>
  </Properties>
  <ClassName>com.google.apigee.edgecallouts.sleeper.SleeperCallout</ClassName>
  <ResourceURL>java://edge-custom-sleeper-1.0.1.jar</ResourceURL>
</JavaCallout>
```

## Example API Proxy

You can find an example proxy bundle that uses the policy, [here in
this repo](bundle/apiproxy).



## Building

Building from source requires Java 1.8, and Maven.

1. unpack (if you can read this, you've already done that).

2. Before building _the first time_, configure the build on your machine by loading the Apigee jars into your local cache:
  ```
  ./buildsetup.sh
  ```

3. Build with maven.
  ```
  mvn clean package
  ```
  This will build the jar and also run all the tests.



## Build Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0

These jars must be available on the classpath for the compile to
succeed. You do not need to worry about these jars if you are not
building from source. The buildsetup.sh script will download the
Apigee files for you automatically, and will insert them into your
maven cache. The pom file will take care of the other Jars.

## Runtime Dependencies

(none)

## Support

This callout is open-source software, and is not a supported part of
Apigee Edge.  If you need assistance, you can try inquiring on [The
Apigee Community Site](https://community.apigee.com).  There is no
service-level guarantee for responses to inquiries regarding this
callout.

## License

This material is Copyright 2019
Google LLC.  and is licensed under the [Apache 2.0
License](LICENSE). This includes the Java code as well as the API
Proxy configuration.

## Bugs

* There are no tests.
