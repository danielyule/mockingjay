package ca.danielyule.mockingjay;

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link MockServer} class when implemented as a Rule. This means
 * that all these tests must pass. See {@link TestAsClass} for tests which fail.
 * 
 * @author Daniel
 * 
 */
public class TestAsRule {

	/**
	 * The port used for listening and connecting to for this test.
	 */
	private static final int TEST_PORT = 12345;

	/**
	 * The bytes that are expected as a default response. Because the server
	 * works best with a request AND response
	 */
	private static final byte[] DEFAULT_RESPONSE = new byte[] { 75, 97, 116, 110, 105, 115, 115 };

	/**
	 * The {@link MockServer} we will be using for testing.
	 */
	@Rule
	public MockServer mockServer = new MockServer(TEST_PORT);

	/**
	 * Tests that the mock server will be considered passing if nothing is done
	 * to it
	 */
	@Test
	// @Ignore
	public void testSendNothing() {
		// Intentionally left blank, but this should still pass and exit
	}

	/**
	 * Tests that the mock server will be considered passing if a socket
	 * connects to it, but no expectations are laid. Because of the way socket
	 * connections work, this may or may not actually connect.
	 * 
	 * @throws UnknownHostException
	 *             If localhost is not defined
	 * @throws IOException
	 *             If there is no networking.
	 */
	@Test
	// @Ignore
	@SuppressWarnings("static-method")
	public void testJustConnect() throws UnknownHostException, IOException {
		Socket s = new Socket("localhost", TEST_PORT);
		s.close();

	}

	/**
	 * Tests the case where the server gets an expectation and then receives the
	 * message it expects.
	 * 
	 * @throws IOException
	 *             If there is a problem writing to the socket.
	 */
	@Test
	// @Ignore
	public void testExpectOneWrittenFirst() throws IOException {
		Writer expectationWriter = new OutputStreamWriter(mockServer.expected());

		expectationWriter.write("I do not like green eggs and ham!");
		expectationWriter.flush();

		mockServer.response().write(DEFAULT_RESPONSE);

		Socket s = new Socket("localhost", TEST_PORT);

		s.getOutputStream().write("I do not like green eggs and ham!".getBytes());

		byte[] received = new byte[DEFAULT_RESPONSE.length];
		Assert.assertThat(s.getInputStream().read(received), is(DEFAULT_RESPONSE.length));
		Assert.assertThat(received, is(DEFAULT_RESPONSE));

		s.close();

	}

	/**
	 * Tests the case where the server receives some data and then is told what
	 * it expects
	 * 
	 * @throws IOException
	 *             If there is a problem writing to the socket.
	 */
	@Test
	// @Ignore
	public void testExpectOneWrittenSecond() throws IOException {
		Writer expectationWriter = new OutputStreamWriter(mockServer.expected());

		Socket s = new Socket("localhost", TEST_PORT);

		s.getOutputStream().write("I do not like green eggs and ham!".getBytes());

		expectationWriter.write("I do not like green eggs and ham!");
		mockServer.response().write(DEFAULT_RESPONSE);
		expectationWriter.flush();

		byte[] received = new byte[DEFAULT_RESPONSE.length];
		Assert.assertThat(s.getInputStream().read(received), is(DEFAULT_RESPONSE.length));
		Assert.assertThat(received, is(DEFAULT_RESPONSE));

		s.close();

	}

	/**
	 * Tests writing two expectation messages sequentially.
	 * 
	 * @throws IOException
	 */
	@Test
	// @Ignore
	public void testExpectTwoWrittenFirst() throws IOException {
		Writer expectationWriter = new OutputStreamWriter(mockServer.expected());

		expectationWriter.write("It was the best of times.  ");
		expectationWriter.flush();
		expectationWriter.write("It was the worst of times.");
		expectationWriter.flush();

		mockServer.response().write(DEFAULT_RESPONSE);

		Socket s = new Socket("localhost", TEST_PORT);

		s.getOutputStream().write(
				"It was the best of times.  It was the worst of times.".getBytes());
		byte[] received = new byte[DEFAULT_RESPONSE.length];
		Assert.assertThat(s.getInputStream().read(received), is(DEFAULT_RESPONSE.length));
		Assert.assertThat(received, is(DEFAULT_RESPONSE));

		s.close();
	}

	/**
	 * Tests writing a pair of messages sequentially.
	 * 
	 * @throws IOException
	 */
	@Test
	// @Ignore
	public void testExpectTwoInterleaved() throws IOException {
		Writer expectationWriter = new OutputStreamWriter(mockServer.expected());

		expectationWriter.write("It was the best of times.  ");
		expectationWriter.flush();
		mockServer.response().write(DEFAULT_RESPONSE);

		expectationWriter.write("It was the worst of times.");
		expectationWriter.flush();
		mockServer.response().write(DEFAULT_RESPONSE);

		Socket s = new Socket("localhost", TEST_PORT);

		s.getOutputStream().write("It was the best of times.  ".getBytes());

		byte[] received = new byte[DEFAULT_RESPONSE.length];
		Assert.assertThat(s.getInputStream().read(received), is(DEFAULT_RESPONSE.length));
		Assert.assertThat(received, is(DEFAULT_RESPONSE));
		
		s.getOutputStream().write("It was the worst of times.".getBytes());
		
		received = new byte[DEFAULT_RESPONSE.length];
		Assert.assertThat(s.getInputStream().read(received), is(DEFAULT_RESPONSE.length));
		Assert.assertThat(received, is(DEFAULT_RESPONSE));
		s.close();
	}

}
