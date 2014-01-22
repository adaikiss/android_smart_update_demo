package com.example.smartupdate.server;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author hlw
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class AjaxResponse {
	private boolean success;
	private String msg;
	private Object data;
	private Integer errorType;
	private ObjectNode json;
	ObjectMapper mapper;

	public AjaxResponse() {
		this.mapper = new ObjectMapper();
		this.json = mapper.createObjectNode();
	}

	public AjaxResponse(boolean success) {
		this();
		setSuccess(success);
	}

	public AjaxResponse(boolean success, String msg) {
		this(success);
		setMsg(msg);
	}

	public AjaxResponse(boolean success, String msg, Object data) {
		this(success, msg);
		setData(data);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getErrorType() {
		return errorType;
	}

	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}

	public void write(Writer writer){
		json.put("success", success);
		json.put("msg", msg);
		json.put("errorType", errorType);
		json.putPOJO("data", data);
		try {
			mapper.writeValue(writer, json);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 输出JSON到response,使用UTF-8编码, 非iframe
	 * 
	 * @param response
	 */
	public void writeJson(HttpServletResponse response) {
		writeJson(response, false);
	}

	/**
	 * 输出JSON到response,使用UTF-8编码
	 * 
	 * @param response
	 */
	public void writeJson(HttpServletResponse response, boolean isIframe) {
		writeJson(response, "UTF-8", isIframe);
	}

	/**
	 * 输出JSON到response
	 * 
	 * @param response
	 * @param encoding
	 * @param isIframe
	 *            对于iframe方式伪ajax,
	 *            避免firefox/chrome添加"&lt;pre&gt;"标签，需要将contentType设置为text/html
	 */
	public void writeJson(HttpServletResponse response, String encoding, boolean isIframe) {
		if (isIframe) {
			response.setContentType("text/html");
		} else {
			response.setContentType("application/json");
		}
		response.setCharacterEncoding(encoding);
		try {
			write(response.getWriter());
		} catch (IOException e) {
			throw new RuntimeException("输出JSON时出现异常！", e);
		}
	}

	@Override
	public String toString(){
		StringWriter writer = new StringWriter();
		write(writer);
		return writer.getBuffer().toString();
	}
}
