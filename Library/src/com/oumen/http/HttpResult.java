package com.oumen.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpResult {
	public static final int RESULT_OK = 0;
	public static final int RESULT_EXCEPTION = 1;
	public static final int RESULT_TIMEOUT_EXCEPTION = 2;
	public static final int RESULT_PARSE_EXCEPTION = 3;
	
	protected HttpResponse response;

	public HttpResult(HttpResponse response) {
		this.response = response;
	}

	public void writeTo(OutputStream os) throws IOException {
		response.getEntity().writeTo(os);
	}
	
	public InputStream getContent() throws IllegalStateException, IOException {
		return response.getEntity().getContent();
	}
	
	public String getResult() throws ParseException, IOException {
		return EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
	}
	
	public String getResult(String charset) throws ParseException, IOException {
		return EntityUtils.toString(response.getEntity(), charset);
	}
}
