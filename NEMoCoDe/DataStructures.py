from abc import abstractmethod


class AccelerometerPacket:
    def __init__(self, id: str, t: float, x: float, y: float, z: float):
        self.id = id
        self.t = t
        self.x = x
        self.y = y
        self.z = z

    def get_id(self):
        return self.id

    def get_time(self):
        return self.t

    def get_x(self):
        return self.x

    def get_y(self):
        return self.y

    def get_z(self):
        return self.z

    def get_accel_data(self):
        return {self.x, self.y, self.z}

    def reset(self):
        self.t = 0


class Package:
    def __init__(self, t: float, packs):
        # packs: AccelerometerPacket[]
        self.t = t
        self.packs = packs


class ImpactData:
    def __init__(self, severity, data):
        self.severity = severity
        self.data = data


class ControllerInterface:
    @abstractmethod
    def get_accelerometer_packet(self, id: str) -> AccelerometerPacket:
        """
        Gets a packet from an accelerometer with a given id
        :param str id: The id of the accelerometer to pull data from
        returns A packet representing x - y - z acceleration at a moment in time
        """
        pass

    @abstractmethod
    def assemble_package(self, packets) -> Package:
        """
        :param Packet[] packets: a group of packets representing a single data collection moment
        :raises An error if the time between packets is not adequately synchronized or ids are different
        :returns A Package created from an array of Packets with synchronized timings
        """
        pass

    @abstractmethod
    def add_package_to_queue(self, pack: Package):
        """
        Adds a Package to the front of the queue and removes the oldest Package from the queue
        :param Package pack: the Package object to be added to the queue
        """
        pass

    @abstractmethod
    def initialize_connection(self, accel_port: int, id: str) -> AccelerometerPacket:
        """
        :param SerialID accel_port: serial id of the specific accelerometer being connected to
        :param str id: ID to set for the accelerometer
        :raises An error if the connection has failed to initialize
        :returns an accelerometer object representing the accelerometer connected to
        """
        pass

    @abstractmethod
    def run_data_collection_loop(self):
        """
        Start collecting data from all of the accelerometers
        Save this data in a circular array / queue type data struct
        Watch for potentially concussive events
        If a concussive event is detected, take a snapshot of the data and send back to user
        """
        pass

    @abstractmethod
    def check_queue_for_concussion(self) -> bool:
        """
        Algorithmic checking of queue for potential concussive impact above threshold limit
        :returns true or false indicating if a concussive event has occurred
        """
        pass

    @abstractmethod
    def create_impact_data(self) -> ImpactData:
        """
        Create an ImpactData object from the current queue data
        This object will contain critical information that the client controller needs to visualize the data
        """
        pass

    @abstractmethod
    def alert_user(self, report: ImpactData):
        """
        Send alert to application with ImpactData for further analysis
        :param ImpactData report: ImpactData object sent to application for further analysis
        """
        pass

    @abstractmethod
    def connect_to_user_device(self) -> bool:
        """
        connects the microcontroller to phone application and initiates mic`rocontroller in standby mode
        @returns a true or false indicating if the connection is successful`
        """
        pass

    @abstractmethod
    def start_session(self):
        """
        Start data collection loop and give diagnostic info to microcontroller/application
        """
        pass

    @abstractmethod
    def run_standby_loop(self):
        """
        Monitor connection with accelerometers for functionality and connection with application for
        start session indication.
        """
        pass


class Controller(ControllerInterface):
    def get_accelerometer_packet(self, id: str) -> AccelerometerPacket:
        pass

    def assemble_package(self, packets) -> Package:
        """
        :param Packet[] packets: a group of packets representing a single data collection moment
        :raises An error if the time between packets is not adequately synchronized or ids are different
        :returns A Package created from an array of Packets with synchronized timings
        """
        packet_time = packets[0].t
        time_tolerance = 0.001
        avg_time = packet_time
        for idx, packet in enumerate(packets):
            if packet.id != idx:
                raise Exception("Packet id's do not match")
            curr_packet_time = packet.t
            if packet_time - time_tolerance >= curr_packet_time or packet_time + time_tolerance <= curr_packet_time:
                raise Exception("Packets are not synchronized correctly")
            avg_time += curr_packet_time
        return Package(avg_time / len(packets), packets)

    def add_package_to_queue(self, pack: Package):
        pass

    def initialize_connection(self, accel_port: int, id: str) -> AccelerometerPacket:
        pass

    def run_data_collection_loop(self):
        pass

    def check_queue_for_concussion(self) -> bool:
        pass

    def create_impact_data(self) -> ImpactData:
        pass

    def alert_user(self, report: ImpactData):
        pass

    def connect_to_user_device(self) -> bool:
        pass

    def start_session(self):
        pass

    def run_standby_loop(self):
        pass
