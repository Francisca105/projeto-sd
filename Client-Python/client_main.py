import sys
from typing import List

from client_service import ClientService as ClientService
from command_processor import CommandProcessor

from debug import Debugger

class ClientMain:
    def main(args: List[str]):
        print("ClientMain")

        Debugger.debug("Debug mode enabled")

        # receive and print arguments
        for i, arg in enumerate(args):
            Debugger.debug(f"arg[{i}] = {arg}")
        Debugger.debug(f"Received {len(args)} arguments")

        # check arguments
        if len(args) < 2:
            print("Argument(s) missing!", file=sys.stderr)
            print("Usage: python3 client_main.py <host:port> <client_id>", file=sys.stderr)
            return

        # get the host and port of the server or front-end
        host_port = args[0]
        client_id = args[1]
        Debugger.debug(f"Client will connect to {host_port} with client_id {client_id}")

        parser = CommandProcessor(ClientService(host_port, client_id))
        parser.parse_input()

if __name__ == "__main__":
    ClientMain.main(sys.argv[1:])