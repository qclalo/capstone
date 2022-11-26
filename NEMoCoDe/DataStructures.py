from collections import deque
from enum import Enum
from math import sqrt
from math import floor
import board
import adafruit_adxl37x
import adafruit_tca9548a
import bluetooth
import subprocess
import sys

"""
I2C Addresses for the accelerometers
"""
DEVICE_ADDRESS = 0x1D
READ_ADDRESS = 0x3B
WRITE_ADDRESS = 0x3A

DEVICE_ADDRESS_ALT = 0x53
READ_ADDRESS_ALT = 0xA7
WRITE_ADDRESS_ALT = 0xA6

"""
Number of accelerometers in use by the device
"""
ACCELEROMETER_COUNT = 5
"""
Data from the accelerometers is in meters per second. Our algorithm uses "g's".
This constant is used to convert meters per second to g's.
"""
GRAVITY_ACCEL_MULTIPLIER = 1 / 9.81

"""
Thresholds for low, medium, and high instantaneous acceleration alerts in g's
"""
ACCEL_THRESHOLD_LOW = 30
ACCEL_THRESHOLD_MEDIUM = 60
ACCEL_THRESHOLD_HIGH = 90

"""
Size, in bytes, of the data transmission from nemocode to app
"""
DATA_TRANSMISSION_SIZE = 1024

"""
Default number of cycles to wait until data is transmitted after receiving a high severity event.
"""
CYCLES = 10

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

class AccelerometerPacket:
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
        self.severity_rating = self.calculate_package_severity()

    def calculate_package_severity(self):
        if (self.max_acceleration >= ACCEL_THRESHOLD_HIGH):
            return Severity.HIGH
        elif (self.max_acceleration >= ACCEL_THRESHOLD_MEDIUM):
            return Severity.MEDIUM
        elif (self.max_acceleration >= ACCEL_THRESHOLD_LOW):
            return Severity.LOW
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
        self.i2c = board.I2C()
        self.multiplexer = adafruit_tca9548a.TCA9548A(self.i2c)

    def get_accelerometer_packet(self, id: int) -> AccelerometerPacket:
        """
        Gets a packet from an accelerometer with a given id
        :param str id: The id of the accelerometer to pull data from
        returns A packet representing x - y - z acceleration at a moment in time
        """
        data = self.accel_ports[id].acceleration
        accel_packet = AccelerometerPacket(id, GRAVITY_ACCEL_MULTIPLIER * data[0], GRAVITY_ACCEL_MULTIPLIER * data[1], GRAVITY_ACCEL_MULTIPLIER * data[2])
        return accel_packet

    def initialize_connection(self, accel_port: int, id: int):
        """
        :param SerialID accel_port: i2c address specific accelerometer being connected to
        :param str id: ID to set for the accelerometer
        :raises An error if the connection has failed to initialize
        :returns an accelerometer object representing the accelerometer connected to
        """
        try:
            accelerometer = adafruit_adxl37x.ADXL375(self.i2c, accel_port)
        except:
            print(f"Failed to connect to i2c device with accel_port={accel_port} and id={id}")
            
        self.accel_ports[id] = accelerometer
        return accelerometer

    def run_data_collection_loop(self):
        """
        Start collecting data from all of the accelerometers
        Save this data in a queue
        Watch for potentially concussive events
        If a concussive event is detected, take a snapshot of the data and send back to user
        """

        packets = []

        for key in self.accel_ports.keys():
            if key == -1:
                # We have the always-on accelerometer,
                # and we can go ahead and read from it.
                packet = self.get_accelerometer_packet(key)
            else:
                # We have a multiplexed accelerometer,
                # and we need to switch to the correct channel.
                if self.multiplexer[key].try_lock():
                    # On successful lock, perform a scan of I2C devices on that channel.
                    addresses = self.multiplexer[key].scan()
                    if DEVICE_ADDRESS in addresses:
                        packet = self.get_accelerometer_packet(key)
                    self.multiplexer[key].unlock()
            packets.append(packet)
        package = Package(self.time, packets)
        self.queue.append(package)
        if package.severity_rating == Severity.HIGH:
            self.alarm_flag = True
        if self.alarm_flag:
            self.cycles_before_report -= 1
        if self.cycles_before_report == 0:
            data = ImpactData(self.queue)
            self.alert_user(data)
            self.alarm_flag = False
            self.cycles_before_report = CYCLES
        self.time += 1

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
        print('Welcome to NEMoCoDe\n')
        print('Attempting to establish connection to always-on accelerometer...\n')
        self.initialize_connection(DEVICE_ADDRESS_ALT, -1)
        print('Attempting to establish connections to multiplexed accelerometers...\n')
        for channel in range(8):
            if self.multiplexer[channel].try_lock():
                self.initialize_connection(DEVICE_ADDRESS, channel)
                self.multiplexer[channel].unlock()
        if len(self.accel_ports) == ACCELEROMETER_COUNT:
            print(f'Successfully established connection with all {ACCELEROMETER_COUNT} accelerometers')
        else:
            print(f'Warning: Failed to establish connection with {ACCELEROMETER_COUNT - len(self.accel_ports)} accelerometers')
        while True:
            self.run_data_collection_loop()
        

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
