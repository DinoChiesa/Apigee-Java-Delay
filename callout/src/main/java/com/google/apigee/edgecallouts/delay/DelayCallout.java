package com.google.apigee.edgecallouts.delay;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.edgecallouts.CalloutBase;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DelayCallout extends CalloutBase implements Execution {

  private static final int DEFAULT_MIN_DELAY_MILLISECONDS = 850;
  private static final int DEFAULT_MAX_DELAY_MILLISECONDS = 1850;
  private static final int MAX_DELAY_MILLISECONDS = 10000;
  private static final SecureRandom secureRandom = new SecureRandom();

  public DelayCallout(Map properties) {
    super(properties);
  }

  public String getVarnamePrefix() {
    return "delay.";
  }

  int randomDelay() {
    return DEFAULT_MIN_DELAY_MILLISECONDS
        + ((int)
            (secureRandom.nextDouble()
                * (DEFAULT_MAX_DELAY_MILLISECONDS - DEFAULT_MIN_DELAY_MILLISECONDS)));
  }

  int getDelay(MessageContext messageContext) throws NumberFormatException {
    String delayValue = getSimpleOptionalProperty("delay", messageContext);
    if (delayValue == null) return randomDelay();
    int millisecondsDelay = Integer.parseInt(delayValue, 10);
    if (millisecondsDelay < 0 || millisecondsDelay > MAX_DELAY_MILLISECONDS) return randomDelay();
    return millisecondsDelay;
  }

  public ExecutionResult execute(
      final MessageContext messageContext, final ExecutionContext executionContext) {
    Instant start = Instant.now();
    messageContext.setVariable(varName("start"), DateTimeFormatter.ISO_INSTANT.format(start));
    int delayMilliseconds = getDelay(messageContext);
    messageContext.setVariable(varName("delay"), Integer.toString(delayMilliseconds));
    executionContext.submitTask(
        new DelayTask(delayMilliseconds, varName("end"), messageContext, executionContext));
    return ExecutionResult.PAUSE;
  }

  private static class DelayTask implements Runnable {
    int delayMilliseconds;
    String variableName;
    MessageContext messageContext;
    ExecutionContext executionContext;

    DelayTask(
        int delayMilliseconds,
        String variableName,
        MessageContext messageContext,
        ExecutionContext executionContext) {
      this.delayMilliseconds = delayMilliseconds;
      this.variableName = variableName;
      this.messageContext = messageContext;
      this.executionContext = executionContext;
    }

    public void run() {
      try {
        Thread.sleep(delayMilliseconds);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

      Instant end = Instant.now();
      messageContext.setVariable(variableName, DateTimeFormatter.ISO_INSTANT.format(end));
      executionContext.resume();
    }
  }
}
