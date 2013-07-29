MockingJay
==========
A Binary Mock Server for JUnit4
-------------------------------

##Introduction
MockingJay is a simple packages for running a mock binary server with JUnit 4. 
This server will cause a test to fail if unexpected data is sent. This server 
can also send data in response to receiving the expected data.
 
##Use
This server is designed to be used as a JUnit rule. In order to use this
server with your JUnit tests, simply add the following to your test suite:
  
    ```Java 
    @Rule
    public MockServer mockServer = new MockServer(TEST_PORT);
    ```

where TEST_PORT is an integer representing the port you expect to receive
data on.

To define what data you expect this server to receive, use the
expected() OutputStream. Any data written to this output
 stream will be expected to be received by the server. For example:
 
    ```Java
    mockServer.expected().write(new byte[] { 1, 2, 3, 4, 5 });
    // put code to be tested here
    ```

This will cause the server to expect the byte sequence [1, 2, 3, 4, 5] from
the socket connection it receives. Because the expected() field
returns an <code>OutputStream</code>, you can use any component of the Java
IO library to write arbitrarily complex data. For example:
 
    ```Java
    Writer expectationWriter = new OutputStreamWriter(mockServer.expected());
    expectationWriter.write(&quot;I do not like green eggs and ham!&quot;);
    ```

To define what the server should respond when it receives data from the
client, use the response() OutputStream. Any data written to
this <code>OutputStream</code> will be sent on the socket as soon as all
expected data up to this point has been sent. Essentially, you should write
expectation and response data to the mock server in the same order you expect
it to be sent along the socket. For example:
 
    ```Java
    mockServer.expected().write(new byte[] { 1, 2, 3, 4, 5 });
    mockServer.response().write(new byte[] { 255, 254, 253, 252 });
    mockServer.expected().write(new byte[] { 6, 7, 8, 9, 10 });
    mockServer.response().write(new byte[] { 251, 250, 249, 248 });
    // put code to be tested here
    ```

This will cause the mock server to expect the sequence of bytes [1, 2, 3, 4, 5],
after it receives this, it will send the bytes [255, 254, 253, 252].
Then, the server will wait for the bytes [6, 7, 8, 9, 10], after which it
will send back [251, 250, 249, 248]. If at any point there is an IO problem,
or the data sent on the socket does not match what the server expects, the
test will fail, although not until the method exits.
 
##Important Note
###Problem
It is strongly recommended that your tests include both an expected and response component.  Because of the implementation of TCP/IP on modern operating systems, if you open a socket to the mock server, write your data and then immediately close, the data may or may not be sent, and the mock server will fail.  If you are getting random, inconsistent test failures, then this may be the cause.
###Solution
Define a response for the mock server and block until you receive the response data, as shown below:

    ```Java
    //Define what we expect the server to send and receive. 
    mockServer.expected().write(new byte[] { 1, 2, 3, 4, 5 });
    mockServer.response().write(new byte[] { 255, 254, 253, 252 });
 
    //Create a socket and connect on the local port
    Socket socket = new Socket('localhost', TEST_PORT);
    socket.connect();
     
    //Send the data to the mock server
    socket.getOutputStream.write(new byte[] { 1, 2, 3, 4, 5 });
  
    byte response = new byte[4];
  
    //block until the server responds
    socket.getInputStream.read(response);
    ``` 
