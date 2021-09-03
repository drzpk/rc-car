#include <string.h>
#include "control_message.h"

const size_t MESSAGE_SIZE = 5;
const size_t MESSAGE_INDICATOR_SIZE = 2;
const char* MESSAGE_INDICATOR = "\xab\xd1";

ControlMessage* parseControlMessage(char* raw) {
  int speed = (int) raw[MESSAGE_INDICATOR_SIZE + 0];
  if (speed > 100 || speed < -100) {
    return 0;
  }

  int direction = (int) raw[MESSAGE_INDICATOR_SIZE + 1];
  if (direction > 100 || direction < -100) {
    return 0;
  }

  int flags = (int) raw[MESSAGE_INDICATOR_SIZE + 2];

  ControlMessage* message = new ControlMessage();
  message->speed = speed / 100.0;
  message->direction = direction / 100.0;
  message->brake = flags & 0x1;
  message->horn = flags & 0x2;
  
  return message;
}
