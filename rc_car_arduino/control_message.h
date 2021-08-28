#ifndef CONTROL_MESSAGE_H
#define CONTROL_MESSAGE_H

#include <WString.h>

extern const size_t MESSAGE_SIZE;
extern const size_t MESSAGE_INDICATOR_SIZE;
extern const char* MESSAGE_INDICATOR;

struct ControlMessage {
  float speed; // [-1, 1]
  float direction; // [-1, 1] - [left, right]
  bool brake;

  String getRepresentation() {
    return "ControlMessage(" + String(speed) + ", " + String(direction) + ")";
  }
};

ControlMessage* parseControlMessage(char* raw);

#endif
