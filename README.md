# Delay callout

This directory contains the Java source code and pom.xml file required to
compile a simple custom policy for Apigee. The policy does one simple thing:
delays for a designated or a random amount of time.

## Why might this be useful?

1. Some people are concerned that an API that generates errors and always
returns naturally, as quickly as possible, may be subject to [remote timing
attacks](https://en.wikipedia.org/wiki/Timing_attack). This callout could
alleviate that concern. You could introduce this callout into the proxy flow,
perhaps in a fault rule, and that would cause the response time to be
randomized, preventing a timnig attack.

2. In cases in which a caller is suspect, or has caused several consecutive errors,
this callout could force a fixed delay, or a backoff delay, before retry.

## Disclaimer

This example is not an official Google product, nor is it part of an
official Google product.

## Using this policy

You do not need to build the source code in order to use the policy in Apigee.
All you need is the built JAR, and the appropriate configuration for the policy.
If you want to build it, feel free.  The instructions are at the bottom of this
readme.


1. copy the jar file, available in target/apigee-custom-delay-20210412.jar , if
   you have built the jar, or in [the
   repo](bundle/apiproxy/resources/java/apigee-custom-delay-20210412.jar) if you
   have not, to your apiproxy/resources/java directory. You can do this offline,
   or using the graphical Proxy Editor in the Apigee Admin UI.

2. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look
   like this:
   ```xml
    <JavaCallout name='Java-Delay-1'>
        ...
      <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
      <ResourceURL>java://apigee-custom-delay-20210412.jar</ResourceURL>
    </JavaCallout>
   ```

3. use the Apigee UI, or a command-line
   tool like [importAndDeploy.js](https://github.com/DinoChiesa/apigee-edge-js/blob/master/examples/importAndDeploy.js)
   or
   [apigeecli](https://github.com/apigee/apigeecli) or similar to
   import the proxy into an Apigee organization, and then deploy the proxy.
   Eg,
   ```sh
   ORG=my-org
   ENV=eval
   node ./importAndDeploy.js -v -o $ORG -e $ENV -d ./bundle
   ```

   or
   ```sh
   ORG=my-org
   ENV=eval
   apigeecli apis create bundle -f ./bundle/apiproxy --name multipart-form -o $ORG  --token $TOKEN
   apigeecli apis deploy --wait --name multipart-form --ovr --rev 1 --org $ORG --env $ENV --token "$TOKEN"
   ```


4. Use a client to generate and send http requests to the proxy you just deployed . Eg,
   ```
   # Apigee Edge
   endpoint=https://$ORG-$ENV.apigee.net
   # Apigee X/hybrid
   endpoint=https://my-custom-endpoint.net
   curl -i "$endpoint/delay/t1"
   ```


## Notes on Usage

There is one callout class, com.google.apigee.callouts.delay.DelayCallout.

The delay time in milliseconds for the policy is configured via a property in the XML. By default it is a random number between 850 and 1850 milliseconds.

## Example: Delay a random amount of time

The callout is compiled to sleep between 850 and 1850 milliseconds. It selects
randomly.

```xml
<JavaCallout name='Java-Delay-1'>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20210412.jar</ResourceURL>
</JavaCallout>
```
## Example: Delay a random amount of time

With this configuration you can specify the minimum and maximum time, and the
callout will select a random value between them, and delay that amount.

```xml
<JavaCallout name='Java-Delay-1'>
  <Properties>
    <!-- delay between 350 and 750 milliseconds -->
    <Property name='delay'>350,750</Property>
  </Properties>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20210412.jar</ResourceURL>
</JavaCallout>
```

## Example: Delay a precise amount of time

```xml
<JavaCallout name='Java-Delay-1'>
  <Properties>
    <!-- delay exactly 4 seconds -->
    <Property name='delay'>4000</Property>
  </Properties>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20210412.jar</ResourceURL>
</JavaCallout>
```

## Example: Delay a time determined by a variable

```xml
<JavaCallout name='Java-Delay-1'>
  <Properties>
    <Property name='delay'>{delayTime}</Property>
  </Properties>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20210412.jar</ResourceURL>
</JavaCallout>
```

## Example API Proxy

You can find an example proxy bundle that uses the policy, [here in
this repo](bundle/apiproxy).


## Building

You don't need to build this callout in order to use it.  But you can build it
if you like. Building from source requires Java 1.8, and Maven.

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

- Apigee expressions v1.0
- Apigee message-flow v1.0

These jars must be available on the classpath for the compile to
succeed. You do not need to worry about these jars if you are not
building from source. The buildsetup.sh script will download the
Apigee files for you automatically, and will insert them into your
maven cache. The pom file will take care of the other Jars.

## Runtime Dependencies

(none)

## Support

This callout is open-source software, and is not a supported part of
Apigee.  If you need assistance, you can try inquiring on [The
Apigee Community Site](https://www.googlecloudcommunity.com/gc/Apigee/bd-p/cloud-apigee).  There is no
service-level guarantee for responses to inquiries regarding this
callout.

## License

This material is Copyright 2019-2023
Google LLC.  and is licensed under the [Apache 2.0
License](LICENSE). This includes the Java code as well as the API
Proxy configuration.

## Bugs

* There are no tests.
