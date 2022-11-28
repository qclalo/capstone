import DataStructures as ds
import time


def main():
    print("Hello World")
    controller = ds.Controller()
    controller.connect_to_user_device()
    controller.start_session()
    #controller.initialize_connection(0x53, 0)
    #for i in range(0, 100):
    #    controller.run_data_collection_loop()
    #data = controller.create_impact_data()
    #controller.alert_user(data)
    #controller.end_session()
    #while True:
    #    controller.run_data_collection_loop()
    #    package = controller.queue[0]
        #print(f'{package.t}: x={package.packs[0].x}, y={package.packs[0].y}, z={package.packs[0].z}, sev={package.severity_rating}')
    #    time.sleep(.01)


if __name__ == '__main__':
    main()
