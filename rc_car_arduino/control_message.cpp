#include <Arduino.h>
#include <string.h>
#include "control_message.h"

const size_t MESSAGE_SIZE = 6;
const size_t MESSAGE_INDICATOR_SIZE = 2;
const char* MESSAGE_INDICATOR = "\xab\xd1";

ControlMessage* parseControlMessage(char* raw) {
  int speed = (int) raw[MESSAGE_INDICATOR_SIZE + 0];
  int direction = (int) raw[MESSAGE_INDICATOR_SIZE + 1];
  int flags = (int) raw[MESSAGE_INDICATOR_SIZE + 2];
  int modifiers = (int) raw[MESSAGE_INDICATOR_SIZE + 3];

  ControlMessage* message = new ControlMessage();
  message->speed = constrain(speed, -100, 100) / 100.0;
  message->direction = constrain(direction, -100, 100) / 100.0;

  message->brake = flags & 0x1;
  message->horn = flags & 0x2;

  message->minimumSpeed = constrain(modifiers >> 4, 0, 10) / 10.0;
  message->maximumTurnRatio = constrain(modifiers & 0xf, 0, 10) / 10.0;
  
  return message;
}
