from collections import deque
from enum import Enum
from math import sqrt
from math import floor
import board
import adafruit_adxl37x
import bluetooth
import subprocess
import sys
import smbus
import time

"""
I3G4250DTR gyroscope
"""

"""
Data from the accelerometers is in meters per second. Our algorithm uses "g's".
This constant is used to convert meters per second to g's.
"""
GRAVITY_ACCEL_MULTIPLIER = 1 / 9.81

"""
Thresholds for low, medium, and high instantaneous acceleration alerts in g's
"""
ROTATION_THRESHOLD_HIGH = 3 * 10 ^ 6

"""
Size, in bytes, of the data transmission from nemocode to app
"""
DATA_TRANSMISSION_SIZE = 1024

"""
Default number of cycles to wait until data is transmitted after receiving a high severity event.
"""
CYCLES = 10

bus = smbus.SMBus(1)

OUT_X_L = 0x28
OUT_X_H = 0x29
OUT_Y_L = 0x2A
OUT_Y_H = 0x2B
OUT_Z_L = 0x2C
OUT_Z_H = 0x2D

bus.write_byte_data(0x28, 0x2A, 0x2C)
bus.write_byte_data(0x29, 0x2B, 0x2D)

time.sleep(0.5)

data0 = bus.read_byte_data()


class Severity(Enum):
    """
    An enum representing different severity scores.
    These severity scores are included within each ImpactData
    """
    UNSET = -1
    MINIMAL = 0
    LOW = 1
    MEDIUM = 2
    HIGH = 3

    """
    OUT_X_L : 0x28
    OUT_X_H : 0x29
    OUT_Y_L : 0x2A
    OUT_Y_H : 0x2B
    OUT_Z_L : 0x2C
    OUT_Z_H : 0x2D
    """



class GyroscopePacket:
    def __init__(self, id: int, x: float, y: float, z: float):
        self.id = id
        self.x = x
        self.y = y
        self.z = z

    def accel_magnitude(self):
        return sqrt((self.x ** 2) + (self.y ** 2) + (self.z ** 2))


class Package:
    def __init__(self, t: float, packs):
        # packs: AccelerometerPacket[]
        self.t = t
        self.packs = packs
        self.max_acceleration = max([pack.accel_magnitude() for pack in packs])

        bus.write_byte_data(0x28, 0x2A, 0x2C)
        bus.write_byte_data(0x29, 0x2B, 0x2D)

        time.sleep(0.5)

        data0 = bus.read_byte_data()

        self.severity_rating = self.calculate_package_severity()

    def calculate_package_severity(self):
        if (data0 >= ROTATION_THRESHOLD_HIGH):
            return Severity.HIGH
        else:
            return Severity.MINIMAL


class ImpactData:
    def __init__(self, data: deque):
        self.data = data
        self.data_size = sys.getsizeof(data)
        first_package = data.popleft()
        package_size = sys.getsizeof(first_package)
        data.appendleft(first_package)
        package_capacity = floor(DATA_TRANSMISSION_SIZE / package_size)
        leftover_bytes = DATA_TRANSMISSION_SIZE % package_size
        """
        If there is more data than we send, trim it.

        Else, do something(?)
        """
        if len(data) > package_capacity:
            diff = len(data) - package_capacity
            i = 0
            while i < diff:
                data.popleft()
                i += 1
        else:
            pass


class Controller:

    def __init__(self):
        self.time = 0
        self.queue = deque([], maxlen = 10)
        self.accel_ports = {}
        self.alarm_flag = False
        self.cycles_before_report = CYCLES
        self.client = None
        self.socket = None

    def run_data_collection_loop(self):
        """
        Start collecting data from the gyroscope
        Watch for potentially concussive events
        If a concussive event is detected, take a snapshot of the data and send back to user
        """
        packets = []
        for index in self.accel_ports:
            packet = self.get_accelerometer_packet(index)
            packets.append(packet)
        package = self.assemble_package(packets)
        self.add_package_to_queue(package)
        if package.severity_rating == Severity.HIGH:
            self.alarm_flag = True
        if self.alarm_flag:
            self.cycles_before_report -= 1
        if self.cycles_before_report == 0:
            data = self.create_impact_data()
            self.alert_user(data)
            self.alarm_flag = False
            self.cycles_before_report = CYCLES
        self.time += 1

    def create_impact_data(self) -> ImpactData:
        """
        Create an ImpactData object from the current queue data
        This object will contain critical information that the client controller needs to visualize the data
        """
        return ImpactData(self.queue)

    def alert_user(self, report: ImpactData):
        """
        Send alert to application with ImpactData for further analysis
        :param ImpactData report: ImpactData object sent to application for further analysis
        """
        while len(report.data) != 0:
            package = report.data.popleft()
            self.client.send(bytes(f'{package.max_acceleration}'.encode('UTF-8')))


    def connect_to_user_device(self):
        """
        connects the microcontroller to phone application and initiates microcontroller in standby mode
        @returns a true or false indicating if the connection is successful`
        """
        result = subprocess.run(['bash', 'bluetooth_settings.sh'], stdout=subprocess.PIPE)
        print(result.stdout.decode('UTF-8'))
        bt_mac = result.stdout.decode('UTF-8').split("hci0")[1].split("BD Address: ")[1].split(" ")[0].strip()
        print(bt_mac)
        port = 5
        backlog = 1
        size = DATA_TRANSMISSION_SIZE
        self.socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.socket.bind((bt_mac, port))
        self.socket.listen(backlog)
        self.client, clientInfo = self.socket.accept()
        print(f'Connected to socket with MAC address = {clientInfo}')

    def start_session(self):
        """
        Start data collection loop and give diagnostic info to microcontroller/application
        """



        pass

    def run_standby_loop(self):
        """
        Monitor connection with accelerometers for functionality and connection with application for
        start session indication.
        """
        pass

    def end_session(self):
        print("Closing sockets")
        if self.client is not None:
            self.client.close()
        if self.socket is not None:
            self.socket.close()
