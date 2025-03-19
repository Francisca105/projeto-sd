package pt.ulisboa.tecnico.tuplespaces.client.grcp;

import com.google.protobuf.ProtocolStringList;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesOuterClass.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.client.ClientMain;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

public class ClientService {
  private ManagedChannel channel;
  private final TupleSpacesBlockingStub stub;
  private final int client_id;

  static final Metadata.Key<String> DELAY_1 = Metadata.Key.of("delay1", Metadata.ASCII_STRING_MARSHALLER);
  static final Metadata.Key<String> DELAY_2 = Metadata.Key.of("delay2", Metadata.ASCII_STRING_MARSHALLER);
  static final Metadata.Key<String> DELAY_3 = Metadata.Key.of("delay3", Metadata.ASCII_STRING_MARSHALLER);

  public ClientService(String host_port, int client_id) {
    // Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
    this.channel = ManagedChannelBuilder.forTarget(host_port).usePlaintext().build();
    ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " connected to " + host_port);

		// It is up to the client to determine whether to block the call.
		// Here we create a blocking stub.
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
    ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " created a blocking stub");

    this.client_id = client_id;
  }

  private TupleSpacesBlockingStub addMetadataToStub(String[] split) {
    Metadata metadata = new Metadata();
    metadata.put(DELAY_1, split[2]);
    metadata.put(DELAY_2, split[3]);
    metadata.put(DELAY_3, split[4]);

    return this.stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor((metadata))); // TODO: check if this is the correct way to add metadata to the stub
  }

  // Adds tuple t to the tuple space
  public void put(String[] split) {
    try {
      String tuple = split[1];
      TupleSpacesBlockingStub _stub = this.stub;
      if (split.length == 5) {
        ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " adding metadata to stub");
        _stub = addMetadataToStub(split);
      }
      ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " putting tuple " + tuple);
      PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
      _stub.put(request);
      ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " put tuple " + tuple);
      System.out.println("OK");

    } catch (StatusRuntimeException e) {
      System.out.println("Server is down. Please try again later.");
      System.out.println(e.toString()); // TODO: DELETE
    }
  }

  // Accepts a tuple description and returns one tuple that matches the description, if it exists.
  // This operation blocks the client until a tuple that satisfies the description exists. The tuple is not removed from the tuple space.
  public String read(String[] split) {
    try {
      String pattern = split[1];
      TupleSpacesBlockingStub _stub = this.stub;
      if (split.length == 5) {
        ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " adding metadata to stub");
        _stub = addMetadataToStub(split);
      }
      ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " reading tuple " + pattern);
      ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
      ReadResponse response = _stub.read(request);
      ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " read tuple with pattern " + pattern + " and got " + response.getResult());

      System.out.println("OK");
      return response.getResult();

    } catch (StatusRuntimeException e) {
      System.out.println("Server is down. Please try again later.");
      System.out.println();
      return null;
    }
  }

  // Accepts a tuple description and returns one tuple that matches the description.
  // This operation blocks the client until a tuple that satisfies the description exists. The tuple is removed from the tuple space.
  // TODO: B.2
  // public String take(String[] split) {
  //   try {
  //     String pattern = split[1];
  //     ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " taking tuple " + pattern);
  //     TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).build();
  //     TakeResponse response = this.stub.take(request);
  //     ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " take tuple with pattern " + pattern + " and got " + response.getResult());

  //     System.out.println("OK");
  //     return response.getResult();

  //   } catch (StatusRuntimeException e) {
  //     System.out.println("Server is down. Please try again later.");
  //     System.out.println();
  //     return null;
  //   }
  // }

  // Does not take arguments and returns a list of all tuples on each server.
  public void getTupleSpacesState() {
    try {
      ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " requested the full tuple space state");
      getTupleSpacesStateRequest request = getTupleSpacesStateRequest.newBuilder().build();
      getTupleSpacesStateResponse response = this.stub.getTupleSpacesState(request);
      ProtocolStringList TupleList = response.getTupleList();

      System.out.println("OK");
      System.out.println(TupleList);
      ClientMain.debug(ClientService.class.getSimpleName(), "Client " + client_id + " received the full tuple space state");

    } catch (StatusRuntimeException e ) {
      System.out.println("Server is down. Please try again later.");
    }
  }

  // A Channel should be shutdown before stopping the process.
  public void shutdown() {
    this.channel.shutdownNow();
  }
}