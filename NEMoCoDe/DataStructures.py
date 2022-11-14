from collections import deque
from enum import Enum
from math import sqrt
from math import floor
import board
import adafruit_adxl37x
import bluetooth
import subprocess
import sys

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
        self.severity_rating = Severity.UNSET

    def get_accel_data(self):
        return {self.x, self.y, self.z}


class Package:
    def __init__(self, t: float, packs):
        # packs: AccelerometerPacket[]
        self.t = t
        self.packs = packs
        if packs == []:
            self.severity_rating = Severity.UNSET
        else:
            self.severity_rating = max([pack.severity_rating for pack in packs])


class ImpactData:
    def __init__(self, severity: Severity, data: deque, data_size: int):
        self.severity = severity
        self.data = data
        self.data_size = sys.getsizeof(data)
        
        package_capacity = floor(DATA_TRANSMISSION_SIZE / sys.getsizeof(data.index(0)))
        leftover_bytes = DATA_TRANSMISSION_SIZE % sys.getsizeof(data.index(0))
        """
        If there is less data than we send, pad it(?) This needs to be changed, waiting on input from Alec regarding bluetooth transmission reqs

        Else if there is more data than we send, trim it.

        Else, do something(?)
        """
        if len(data) < package_capacity:
            diff = package_capacity - len(data)
            junk_package = Package(-1, [])
            i = 0
            while i < diff:
                data.appendleft(junk_package)
                i += 1
        elif len(data) > package_capacity:
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

    def get_accelerometer_packet(self, id: int) -> AccelerometerPacket:
        """
        Gets a packet from an accelerometer with a given id
        :param str id: The id of the accelerometer to pull data from
        returns A packet representing x - y - z acceleration at a moment in time
        """
        data = self.accel_ports[id].acceleration
        accel_packet = AccelerometerPacket(id, GRAVITY_ACCEL_MULTIPLIER * data[0], GRAVITY_ACCEL_MULTIPLIER * data[1], GRAVITY_ACCEL_MULTIPLIER * data[2])
        return accel_packet


    def calculate_vector_length(self, x: float, y: float, z: float):
        return sqrt((x ** 2) + (y ** 2) + (z ** 2))

    def assign_packet_severity(self, packet: AccelerometerPacket):
        accel_magnitude = self.calculate_vector_length(packet.x, packet.y, packet.z)
        if (accel_magnitude >= ACCEL_THRESHOLD_HIGH):
            packet.severity_rating = Severity.HIGH
        elif (accel_magnitude >= ACCEL_THRESHOLD_MEDIUM):
            packet.severity_rating = Severity.MEDIUM
        elif (accel_magnitude >= ACCEL_THRESHOLD_LOW):
            packet.severity_rating = Severity.LOW
        else:
            packet.severity_rating = Severity.MINIMAL


    def assemble_package(self, packets) -> Package:
        """
        :param Packet[] packets: a group of packets representing a single data collection moment
        :returns A Package created from an array of Packets with synchronized timings
        """
        packets.sort(key = lambda x: x.id)
        return Package(self.time, packets)

    def add_package_to_queue(self, pack: Package):
        """
        Adds a Package to the back of the queue and removes the oldest Package from the front of the queue, if the queue is full.
        :param Package pack: the Package object to be added to the queue
        """
        self.queue.append(pack)

    def initialize_connection(self, accel_port: int, id: int):
        """
        :param SerialID accel_port: i2c address specific accelerometer being connected to
        :param str id: ID to set for the accelerometer
        :raises An error if the connection has failed to initialize
        :returns an accelerometer object representing the accelerometer connected to
        """
        try:
            i2c = board.I2C()
            accelerometer = adafruit_adxl37x.ADXL375(i2c, accel_port)
        except: 
            raise Exception(f"Failed to connect to i2c device with accel_port={accel_port} and id={id}")
        self.accel_ports[id] = accelerometer
        return accelerometer

    def run_data_collection_loop(self):
        """
        Start collecting data from all of the accelerometers
        Save this data in a circular array / queue type data struct
        Watch for potentially concussive events
        If a concussive event is detected, take a snapshot of the data and send back to user
        """
        packets = []
        for index in self.accel_ports:
            packet = self.get_accelerometer_packet(index)
            self.assign_packet_severity(packet)
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
        last_element_in_queue = self.queue.pop()
        severity_rating = last_element_in_queue.severity_rating
        self.queue.append(last_element_in_queue)
        return ImpactData(severity_rating, self.queue)

    def alert_user(self, report: ImpactData):
        """
        Send alert to application with ImpactData for further analysis
        :param ImpactData report: ImpactData object sent to application for further analysis
        """
        pass

    def connect_to_user_device(self) -> bool:
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
        socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        socket.bind((bt_mac, port))
        socket.listen(backlog)
        try:
            client, clientInfo = socket.accept()
            while 1:
                data = client.recv(size)
                if data:
                    print(data)
                    client.send(data)
        except:
            print("Closing socket")
            client.close()
            socket.close()


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
