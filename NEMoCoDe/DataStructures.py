from collections import deque
from enum import Enum
from math import sqrt

"""
Intended to function as a global constant that indicates a concussion has occurred once this value is surpassed.
Until we better understand the data format of the accelerometers then this will be a dummy number.
"""
ACCEL_THRESHOLD_LOW = 30
ACCEL_THRESHOLD_MEDIUM = 60
ACCEL_THRESHOLD_HIGH = 90

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

    def get_accel_data(self):
        return {self.x, self.y, self.z}


class Package:
    def __init__(self, t: float, packs):
        # packs: AccelerometerPacket[]
        self.t = t
        self.packs = packs


class ImpactData:
    def __init__(self, severity: Severity, data: deque):
        self.severity = severity
        self.data = data


class Controller:

    def __init__(self, time: int, queue: deque, accel_ports):
        self.time = 0
        self.queue = deque([], maxlen = 10000)
        self.accel_ports = []

    def get_accelerometer_packet(self, id: int) -> AccelerometerPacket:
        """
        Gets a packet from an accelerometer with a given id
        :param str id: The id of the accelerometer to pull data from
        returns A packet representing x - y - z acceleration at a moment in time
        """
        pass

    def calculate_vector_length(self, x: float, y: float, z: float):
        return sqrt((x ** 2) + (y ** 2) + (z ** 2))

    def check_packet_for_concussive_event(self, packet: AccelerometerPacket) -> Severity:
        accel_magnitude = self.calculate_vector_length(packet.x, packet.y, packet.z)
        if (accel_magnitude >= ACCEL_THRESHOLD_HIGH):
            return Severity.HIGH
        elif (accel_magnitude >= ACCEL_THRESHOLD_MEDIUM):
            return Severity.MEDIUM
        elif (accel_magnitude >= ACCEL_THRESHOLD_LOW):
            return Severity.LOW
        else:
            return Severity.MINIMAL


    def assemble_package(self, packets) -> Package:
        """
        :param Packet[] packets: a group of packets representing a single data collection moment
        :returns A Package created from an array of Packets with synchronized timings
        """
        packets.sort(key = packets.id)
        return Package(self.time, packets)

    def add_package_to_queue(self, pack: Package):
        """
        Adds a Package to the front of the queue and removes the oldest Package from the rear of the queue, if the queue is full.
        :param Package pack: the Package object to be added to the queue
        """
        self.queue.appendleft(pack)

    def initialize_connection(self, accel_port: int, id: int) -> AccelerometerPacket:
        """
        :param SerialID accel_port: serial id of the specific accelerometer being connected to
        :param str id: ID to set for the accelerometer
        :raises An error if the connection has failed to initialize
        :returns an accelerometer object representing the accelerometer connected to
        """
        pass

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
            packets.append(packet)
        package = self.assemble_package(packets)
        self.add_package_to_queue(package)
        if(self.check_package_for_concussion(package)):
            data = self.create_impact_data()
            self.alert_user(data)

    def check_package_for_concussion(self, package: Package) -> bool:
        """
        Algorithmic checking of queue for potential concussive impact above threshold limit
        :returns true or false indicating if a concussive event has occurred
        """
        for pack in package.packs:
            for val in pack.get_accel_data():
                if (val >= ACCEL_THRESHOLD):
                    return True
        return False

    def create_impact_data(self) -> ImpactData:
        """
        Create an ImpactData object from the current queue data
        This object will contain critical information that the client controller needs to visualize the data
        """
        pass

    def alert_user(self, report: ImpactData):
        """
        Send alert to application with ImpactData for further analysis
        :param ImpactData report: ImpactData object sent to application for further analysis
        """
        pass

    def connect_to_user_device(self) -> bool:
        """
        connects the microcontroller to phone application and initiates mic`rocontroller in standby mode
        @returns a true or false indicating if the connection is successful`
        """
        pass

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
