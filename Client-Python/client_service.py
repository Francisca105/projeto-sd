import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')

import grpc
from TupleSpaces_pb2 import PutRequest, ReadRequest, ReadResponse, TakeRequest, TakeResponse, getTupleSpacesStateRequest
from TupleSpaces_pb2_grpc import TupleSpacesStub

class ClientService:
    def __init__(self, host_port: str, client_id: int):
        # Create a channel to connect to the server (plaintext communication)
        self.channel = grpc.insecure_channel(host_port)

        # Create a blocking stub for synchronous calls
        self.stub = TupleSpacesStub(self.channel)

    # Adds tuple t to the tuple space
    def put(self, tuple_str: str):
        request = PutRequest(newTuple=tuple_str)
        self.stub.put(request)

    # Reads a tuple without removing it from the tuple space
    def read(self, pattern: str) -> str:
        request = ReadRequest(searchPattern=pattern)
        response: ReadResponse = self.stub.read(request)
        return response.result

    # Takes a tuple, removing it from the tuple space
    def take(self, pattern: str) -> str:
        request = TakeRequest(searchPattern=pattern)
        response: TakeResponse = self.stub.take(request)
        return response.result

    # Gets the full tuple space state
    def get_tuple_spaces_state(self):
        request = getTupleSpacesStateRequest()
        response = self.stub.getTupleSpacesState(request)
        tuples_list = list(response.tuple)

        print("[", end="")
        for i in range(len(tuples_list)):
            tuples_list[i] = tuples_list[i][1:-1]
            print(f"<{tuples_list[i]}>", end="")
            if i != len(tuples_list) - 1:
                print(", ", end="")
        print("]")

    # Shuts down the channel
    def shutdown(self):
        self.channel.close()