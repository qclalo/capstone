import DataStructures as ds
import time

def main():
    print("Hello World")
    controller = ds.Controller()
    controller.connect_to_user_device()
    controller.initialize_connection(0x53, 0)
    while True:
        controller.run_data_collection_loop()
        package = controller.queue[0]

        print(f'{package.t}: x={package.packs[0].x}, y={package.packs[0].y}, z={package.packs[0].z}, sev={package.severity_rating}')
        #ap = controller.get_accelerometer_packet(0)
        #print(f"x={ap.x}, y={ap.y}, z={ap.z}")
        time.sleep(.01)


if __name__ == '__main__':
    main()
