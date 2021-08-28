#ifndef MOTOR_DRIVER_H
#define MOTOR_DRIVER_H

#include "control_message.h"

void initializeMotorDriver();
void processMessage(ControlMessage* msg);

#endif
