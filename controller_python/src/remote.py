import bluetooth
import keyboard
import time

SPEED_VALUE = 50
DIRECTION_VALUE = 100


class ControlMessage:
    _MESSAGE_INDICATOR = b"\xab\xd1"
    speed: int
    direction: int
    brake: bool
    horn: bool

    def __init__(self):
        self.speed = 0
        self.direction = 0
        self.brake = False
        self.horn = False

    def encode(self) -> bytes:
        flags = int(self.brake) | (int(self.horn) << 1)

        return self._MESSAGE_INDICATOR \
               + self.speed.to_bytes(1, byteorder='big', signed=True) \
               + self.direction.to_bytes(1, byteorder='big', signed=True) \
               + flags.to_bytes(1, byteorder='big', signed=True)

    def __str__(self):
        return f"ControlMessage({self.speed}, {self.direction})"


def get_device_mac():
    with open("mac.txt") as handle:
        return handle.readline()


def create_message():
    if keyboard.is_pressed("q"):
        return None

    message = ControlMessage()
    if keyboard.is_pressed("up arrow"):
        message.speed = 40
    elif keyboard.is_pressed("down arrow"):
        message.speed = -60

    if keyboard.is_pressed("left arrow"):
        message.direction = -DIRECTION_VALUE
    elif keyboard.is_pressed("right arrow"):
        message.direction = DIRECTION_VALUE

    if keyboard.is_pressed("h"):
        message.horn = True

    return message


def main():
    socket = bluetooth.BluetoothSocket()
    socket.connect((get_device_mac(), 1))

    while True:
        message = create_message()
        if not message:
            break

        encoded = message.encode() + b"\n"
        socket.send(encoded)
        print("\r" + str(message) + " " * 20, end="")
        time.sleep(0.4)


if __name__ == "__main__":
    main()
