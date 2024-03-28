# Apigee Delay callout

This directory contains the Java source code and pom.xml file required to
compile a simple custom policy for Apigee. The policy does one simple thing:
delays for a designated or a random amount of time.

## Why might this be useful?

1. Some people are concerned that an API that generates errors and always
returns naturally, as quickly as possible, may be subject to [remote timing
attacks](https://en.wikipedia.org/wiki/Timing_attack). This callout could
alleviate that concern. You could introduce this callout into the proxy flow,
perhaps in a fault rule, and that would cause the response time to be
randomized, preventing a timing attack.

2. In cases in which a caller is suspect, or has caused several consecutive
errors, or is calling too often, this callout could force a fixed delay, or a
backoff delay, or a random delay, before Apigee sends the response.

How to determine if a caller is "calling too much" is up to you. You can use the
Quota policy with a `continueOnError='true'` as one way to do that. Or a
`CountOnly` element.


## Disclaimer

This example is not an official Google product, nor is it part of an
official Google product.

## Using this policy in your API proxy

You do not need to build the source code in order to use the policy in Apigee.
All you need is the built JAR, and the appropriate configuration for the policy.
If you want to build it, feel free.  The instructions are at the bottom of this
readme.


1. copy the jar file, available in target/apigee-custom-delay-20240327.jar , if
   you have built the jar, or in [the
   repo](bundle/apiproxy/resources/java/apigee-custom-delay-20240327.jar) if you
   have not, to your apiproxy/resources/java directory. You can do this offline,
   or using the graphical Proxy Editor in the Apigee Admin UI.

2. include an XML file for the Java callout policy in your
   apiproxy/resources/policies directory. It should look
   like this:
   ```xml
    <JavaCallout name='Java-Delay-1'>
        ...
      <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
      <ResourceURL>java://apigee-custom-delay-20240327.jar</ResourceURL>
    </JavaCallout>
   ```

3. use the Apigee UI, or a command-line
   tool like [apigeecli](https://github.com/apigee/apigeecli) or similar to
   import the proxy into an Apigee organization, and then deploy the proxy.
   Eg,
   ```sh
   ORG=my-org
   ENV=my-environment
   TOKEN=$(gcloud auth print-access-token)
   apigeecli apis create bundle -f ./bundle/apiproxy --name my-proxy-name -o $ORG  --token $TOKEN
   apigeecli apis deploy --wait --name my-proxy-name --ovr --org $ORG --env $ENV --token $TOKEN
   ```


4. Use a client to generate and send http requests to the proxy you just deployed . Eg,
   ```
   endpoint=https://my-custom-endpoint.net
   curl -i "$endpoint/delay/t1"
   ```


## Notes on Usage

There is one callout class, com.google.apigee.callouts.delay.DelayCallout.

The delay time in milliseconds for the policy is configured via a property in
the XML.

### Example: Delay a specific amount of time

This configuration is the simplest - it tells the callout to delay a specific
amount of time: 4 seconds.

```xml
<JavaCallout name='Java-Delay-1'>
  <Properties>
    <!-- delay exactly 4000 milliseconds -->
    <Property name='delay'>4000</Property>
  </Properties>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20240327.jar</ResourceURL>
</JavaCallout>
```

The largest delay the callout allows is 30 seconds. If
you specify an out of range value, the callout will delay a "default" amount of
time.

### Example: Delay a random amount of time, given specific min and max

With this configuration you can specify the minimum and maximum time, and the
callout will select a random value between them, and delay that amount. It's non-deterministic.

```xml
<JavaCallout name='Java-Delay-1'>
  <Properties>
    <!-- delay between 350 and 750 milliseconds -->
    <Property name='delay'>350,750</Property>
  </Properties>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20240327.jar</ResourceURL>
</JavaCallout>
```


### Example: Delay a time determined by a variable

In this case , the callout delays according to what is stored in a variable.
The content of the variable could be either an integer, or a pair of integers
separated by commas, for a min and max as described above.

```xml
<JavaCallout name='Java-Delay-1'>
  <Properties>
    <Property name='delay'>{delayTime}</Property>
  </Properties>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20240327.jar</ResourceURL>
</JavaCallout>
```

### Example: Delay a random amount of time, subject to the defaults

With no specified configuration, the callout is compiled to sleep between 850
and 1850 milliseconds. It selects randomly.

```xml
<JavaCallout name='Java-Delay-1'>
  <ClassName>com.google.apigee.callouts.delay.DelayCallout</ClassName>
  <ResourceURL>java://apigee-custom-delay-20240327.jar</ResourceURL>
</JavaCallout>
```

### Further Notes

The callout does not help you to determine IF you want to delay a response.
There is no persistence or state managed by this callout. For that you would
need to combine it with a different policy, like the Quota policy that uses a
`CountOnly` element.

For example:

```
<Step>
  <Name>Quota-CountOnly</Name>
</Step>
<Step>
  <Name>Java-Delay</Name>
  <Condition>ratelimit.Quota-CountOnly.exceed.count > 0</Condition>
</Step>
```

This would delay if and only if the Quota policy determined the caller had
reached its soft limit. You could still have a separate "hard limit" Quota
policy that returned a 429.


## Example API Proxy

You can find an example proxy bundle that uses the policy, [here in
this repo](bundle/apiproxy). It is a loopback proxy; it only executes the Java callout and returns.

To use it:

1. import and deploy the example proxy with [apigeecli](https://github.com/apigee/apigeecli) or a similar tool.
   Eg,
   ```sh
   ORG=my-org
   ENV=my-environment
   TOKEN=$(gcloud auth print-access-token)
   apigeecli apis create bundle -f ./bundle/apiproxy --name delay -o $ORG  --token $TOKEN
   apigeecli apis deploy --wait --name delay --ovr --org $ORG --env $ENV --token $TOKEN
   ```

2. Use a client to generate and send http requests to the proxy you just deployed . Eg,
   ```
   endpoint=https://my-custom-endpoint.net

   # delay a fixed 1500ms for each response
   curl -i "$endpoint/delay/t1"

   # delay a random amount of time between 100 and 300 ms
   curl -i "$endpoint/delay/t2"

   # delay a random amount of time between 1000 and 3000 ms
   curl -i "$endpoint/delay/t3"
   ```


## Building

You don't need to build this callout in order to use it.  But _you can build it
if you like_. Building from source requires Java 1.8 (specifically), and Maven 3.5 or later.

1. unpack (if you can read this, you've already done that).

2. Before building _the first time_, configure the build on your machine by
   loading the Apigee jars into your local cache:

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
succeed. The [buildsetup.sh](./buildsetup.sh) script will download the
Apigee files for you automatically, and will insert them into your
maven cache.

You do not need to worry about these jars if you are not
building from source.

## Runtime Dependencies

(none)

## Support

This callout is open-source software, and is not a supported part of Apigee.  If
you need assistance, you can try inquiring on [The Apigee Community
Site](https://www.googlecloudcommunity.com/gc/Apigee/bd-p/cloud-apigee).  There
is no service-level guarantee for responses to inquiries regarding this callout.

## License

This material is Copyright 2019-2024 Google LLC.  and is licensed under the
[Apache 2.0 License](LICENSE). This includes the Java code as well as the API
Proxy configuration.

## Bugs

* There are no tests.
