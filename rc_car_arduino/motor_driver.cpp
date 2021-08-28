#include "motor_driver.h"

#include <Arduino.h>

const int LEFT_MOTOR_FORWARD_PIN = 4;
const int LEFT_MOTOR_REVERSE_PIN = 2;
const int LEFT_MOTOR_PWM_PIN = 3;

const int RIGHT_MOTOR_FORWARD_PIN = 7;
const int RIGHT_MOTOR_REVERSE_PIN = 5;
const int RIGHT_MOTOR_PWM_PIN = 6;

// Minimum speed required to start the motor
const float MINIMUM_SPEED = 0.6;
// Maximum time wihtout signal after which motors are stopped;
const int MAX_INACTIVE_TIME = 500;
// Time to ignore control messages after max inactive time has been reached.
// This is to prevent from stuttering when message frequency is just above the inactivity time.
const int COOLDOWN_TIME = 2000;
// Maximum fraction of motor power that can be taken away from it when turning.
const float MAX_TURN_RATIO = 0.6;

bool working = true;
unsigned long lastTime = 0;
unsigned long lastProcessTime = 0;
unsigned long cooldownAt = 0;


void doProcessMessage(ControlMessage& msg);
void stopMotors();
void setSignalsWhenMoving(ControlMessage& msg);
void setSignalsWhenOnlyTurning(ControlMessage& msg);
void setPins(int leftPwm, int leftForward, int leftReverse,
             int rightPwm, int rightForward, int rightReverse);

void initializeMotorDriver() {
  pinMode(LEFT_MOTOR_FORWARD_PIN, OUTPUT);
  pinMode(LEFT_MOTOR_REVERSE_PIN, OUTPUT);
  pinMode(LEFT_MOTOR_PWM_PIN, OUTPUT);
  
  pinMode(RIGHT_MOTOR_FORWARD_PIN, OUTPUT);
  pinMode(RIGHT_MOTOR_REVERSE_PIN, OUTPUT);
  pinMode(RIGHT_MOTOR_PWM_PIN, OUTPUT);

  setPins(0, HIGH, LOW, 0, HIGH, LOW);

  lastTime = millis();
}

void processMessage(ControlMessage* msg) {
  unsigned long currentProcessTime = millis();
  if (cooldownAt > currentProcessTime) {
    Serial.println("Cooldown...");
    return;
  }
  
  if (msg) {
    working = true;
    lastProcessTime = currentProcessTime;
    doProcessMessage(*msg);
  } else if (working && (currentProcessTime - lastProcessTime) > MAX_INACTIVE_TIME) {
    working = false;
    stopMotors();
    cooldownAt = millis() + COOLDOWN_TIME;
  }
}

void doProcessMessage(ControlMessage& msg) {
  if (msg.brake) {
    stopMotors();
  } else {
    setSignalsWhenMoving(msg);
  }
}

void stopMotors() {
  setPins(255, LOW, LOW, 255, LOW, LOW);
}

// Decrease speed of one motor
void setSignalsWhenMoving(ControlMessage& msg) {
  // Leave forward in HIGH state by default to prevent from fast braking
  int leftPwm = 0;
  int rightPwm = 0;
  int forward = HIGH;
  int reverse = LOW;

  float power = 0;
  if (msg.speed != 0) {
    power = MINIMUM_SPEED + (1.0 - MINIMUM_SPEED) * abs(msg.speed);
  }
  
  leftPwm = rightPwm = floor(255 * power);
  if (msg.speed < 0) {
    forward = LOW;
    reverse = HIGH;
  }

  float pwmDelta = floor(abs(msg.direction) * leftPwm * MAX_TURN_RATIO);
  leftPwm -= msg.direction < 0 ? pwmDelta : 0;
  rightPwm -= msg.direction > 0 ? pwmDelta : 0;

  if (leftPwm < 255 * MINIMUM_SPEED) {
    leftPwm = 0;
  }
  if (rightPwm < 255 * MINIMUM_SPEED) {
    rightPwm = 0;
  }

  setPins(leftPwm, forward, reverse, rightPwm, forward, reverse);
}

// Turn motors in opposite directions
void setSignalsWhenOnlyTurning(ControlMessage& message) {
  // todo: ensure that this function can only be called when motors have stopped
}

void setPins(int leftPwm, int leftForward, int leftReverse,
             int rightPwm, int rightForward, int rightReverse) {

  analogWrite(LEFT_MOTOR_PWM_PIN, leftPwm);
  analogWrite(RIGHT_MOTOR_PWM_PIN, rightPwm);

  digitalWrite(LEFT_MOTOR_FORWARD_PIN, leftForward);
  digitalWrite(LEFT_MOTOR_REVERSE_PIN, leftReverse);

  digitalWrite(RIGHT_MOTOR_FORWARD_PIN, rightForward);
  digitalWrite(RIGHT_MOTOR_REVERSE_PIN, rightReverse);
}
