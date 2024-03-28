// Copyright 2019-2024 Google LLC.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.google.apigee.callouts.delay;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.callouts.CalloutBase;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DelayCallout extends CalloutBase implements Execution {

  private static final int DEFAULT_MIN_DELAY_MILLISECONDS = 850;
  private static final int DEFAULT_MAX_DELAY_MILLISECONDS = 1850;
  private static final int MAX_DELAY_MILLISECONDS = 30000;
  private static final SecureRandom secureRandom = new SecureRandom();

  public DelayCallout(Map properties) {
    super(properties);
  }

  public String getVarnamePrefix() {
    return "delay.";
  }

  int randomDelay() {
    return randomDelay(DEFAULT_MIN_DELAY_MILLISECONDS, DEFAULT_MAX_DELAY_MILLISECONDS);
  }

  int randomDelay(int min, int max) {
    return min + ((int) (secureRandom.nextDouble() * (max - min)));
  }

  int getDelay(MessageContext messageContext) throws NumberFormatException {
    String delayValue = getSimpleOptionalProperty("delay", messageContext);
    if (delayValue == null) return randomDelay();
    if (delayValue.indexOf(",") > 0) {
      String[] parts = delayValue.split(",", 2);
      if (parts.length != 2) return randomDelay();
      int min = Integer.parseInt(parts[0], 10);
      int max = Integer.parseInt(parts[1], 10);
      if (min >= max) return randomDelay();
      if (min < 0 || max < 0) return randomDelay();
      return randomDelay(min, max);
    } else {
      int millisecondsDelay = Integer.parseInt(delayValue, 10);
      if (millisecondsDelay < 0 || millisecondsDelay > MAX_DELAY_MILLISECONDS) return randomDelay();
      return millisecondsDelay;
    }
  }

  public ExecutionResult execute(
      final MessageContext messageContext, final ExecutionContext executionContext) {
    messageContext.setVariable(
        varName("start"), DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    int delayMilliseconds = getDelay(messageContext);
    messageContext.setVariable(varName("delay"), Integer.toString(delayMilliseconds));
    executionContext.scheduleTask(
        () -> {
          messageContext.setVariable(
              varName("end"), DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
          executionContext.resume();
          return;
        },
        delayMilliseconds,
        TimeUnit.MILLISECONDS);

    return ExecutionResult.PAUSE;
  }
}
