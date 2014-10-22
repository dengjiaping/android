package com.oumen.message.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.oumen.android.util.Constants;
import com.oumen.message.MessageFactory;
import com.oumen.tools.ELog;

public class MessageConnection {
	public static final int CONNECT_RESULT_OK = 0;
	public static final int CONNECT_RESULT_CONNECTED = 1;
	public static final int CONNECT_RESULT_EXCEPTION = 2;
	
	private final ExecutorService THREAD = Executors.newFixedThreadPool(2);
	
	public static final MessageConnection instance = new MessageConnection();
	
	private AtomicBoolean retry = new AtomicBoolean(false);
	private AtomicBoolean forceClose = new AtomicBoolean(false);
	
	private MessageConnection() {}
	
	private Socket socket;
	
	private final Input input = new Input();
	private final Output output = new Output();
	
	private ConnectionListener connectionListener;
	
	synchronized public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	synchronized public boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	synchronized public boolean isRetry() {
		return retry.get();
	}
	
	synchronized public int connect() throws InterruptedException, ExecutionException {
		int result = CONNECT_RESULT_OK;
		try {
			ELog.e("Connecting in " + Thread.currentThread());
			retry.set(true);
			socket = new Socket();
			socket.setKeepAlive(true);
			socket.connect(new InetSocketAddress(Constants.SOCKET_SERVER_IP, Constants.SOCKET_SERVER_PORT), 5000);
			input.is = socket.getInputStream();
			output.os = socket.getOutputStream();
			THREAD.execute(input);
			THREAD.execute(output);
		}
		catch (Exception e) {
			ELog.e("Exception:" + e.getMessage());
			e.printStackTrace();
			result = CONNECT_RESULT_EXCEPTION;
			
			close(true, false);
		}
		return result;
	}
	
	synchronized public void send(String msg) {
		try {
			//将指定元素插入到此队列的尾部，如有必要，则等待空间变得可用。
			output.msgs.put(msg);
		}
		catch (InterruptedException e) {
			//如果等待时被中断
			e.printStackTrace();
			connectionListener.onSendFailed(msg, e);
		}
		catch (Exception e) {
			e.printStackTrace();
			if (isConnected()) {
				close(true, false);
			}
			connectionListener.onSendFailed(msg, e);
		}
	}
	
	synchronized public void close(boolean isRetry, boolean isForceClose) {
		retry.set(isRetry);
		forceClose.set(isForceClose);
		
		if (socket != null && !socket.isClosed()) {
			try {
				socket.close();
			}
			catch (IOException e) {}
			socket = null;
		}
		
		if (input.is != null) {
			try {
				input.is.close();
			} catch (IOException e) {}
			input.is = null;
		}
		
		if (output.os != null) {
			try {
				output.msgs.clear();
				output.os.close();
			} catch (IOException e) {}
			output.os = null;
		}
		ELog.w("isConnected:" + isConnected() + " isRetry:" + retry.get());
	}
	
	synchronized public boolean isInitialized() {
		return socket != null && input.is != null && output.os != null;
	}
	
	private class Input implements Runnable {
		InputStream is;

		@Override
		public void run() {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream(10240);
			int in = -1;
			boolean isBegin = false;
			try {
				while((in = is.read()) != -1) {
					if (in == '\r') {
						process(buffer.toByteArray());
						buffer.reset();//清空
						isBegin = false;
					}
					else if (!isBegin && in == '{') {
						isBegin = true;
						buffer.write(in);
					}
					else if (isBegin) {
						buffer.write(in);
					}
				}
			}
			catch (Exception e) {
				ELog.e("Exception:" + e.getMessage());
				if (isConnected() && !forceClose.get()) {
					close(true, false);
				}
				e.printStackTrace();
			}
		}
		
		private void process(byte[] data) {
			try {
				String msg = new String(data, "GBK");
				if (msg.startsWith("{{")) {
					msg = msg.substring(1);
				}
				ELog.i("Msg:" + msg);
				
				if (connectionListener != null) {
					connectionListener.input(MessageFactory.create(msg));
				}
				else {
					ELog.e("MessageListener is null!");
				}
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class Output implements Runnable {
		final LinkedBlockingQueue<String> msgs = new LinkedBlockingQueue<String>();
		OutputStream os;

		@Override
		public void run() {
			while (isConnected()) {
				String msg = null;
				try {
					msg = msgs.poll(5, TimeUnit.SECONDS);
					if (msg == null) {
						continue;
					}
					
					byte[] data = wrapData(msg);
					os.write(data);
					os.flush();
					ELog.i("Length:" + data.length + " Msg:" + msg);
					
					msg = null;
				}
				catch (Exception e) {
					ELog.e("Exception:" + e.getMessage());
					e.printStackTrace();
					
					if (isConnected()) {
						close(false, false);
					}
					
					if (msg != null) {
						connectionListener.onSendFailed(msg, e);
					}
				}
			}
		}
		
		private byte[] wrapData(String msg) throws UnsupportedEncodingException {
			byte[] msgBytes = msg.getBytes("GBK");
			
			byte[] lengthBytes = new byte[4];
			lengthBytes[0] = (byte)((msgBytes.length >> 24) & 0xFF);
			lengthBytes[1] = (byte)((msgBytes.length >> 16) & 0xFF);
			lengthBytes[2] = (byte)((msgBytes.length >> 8) & 0xFF);
			lengthBytes[3] = (byte)(msgBytes.length & 0xFF);

			byte[] data = new byte[msgBytes.length + 7];
			for (int i = 0; i < lengthBytes.length; i++) {
				data[2 + i] = lengthBytes[i];
			}
			for (int i = 0; i < msgBytes.length; i++) {
				data[6 + i] = msgBytes[i];
			}
			data[data.length - 1] = '\r';
			return data;
		}
	}
}
