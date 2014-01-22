package com.example.smartupdate.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet("/upgrade")
public class UpgradeServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String version = req.getParameter("version");
		System.out.println(version);
		AjaxResponse res = new AjaxResponse(true);
		Map<String, Object> data = new HashMap<String, Object>();
		res.setData(data);
		if("1.1".equals(version)){
			data.put("upgrade", false);
		}else{
			data.put("upgrade", true);
			data.put("version", "1.1");
			data.put("patch", true);
			data.put("sha1", "1b2d18daafae4cb136a78220810a0b7950e35b97");
			data.put("patch_sha1", "793daa22331cf6afe5c90f0d810c4beb443507d3");
			data.put("url", "http://192.168.1.104/1.0-1.1.patch");
			data.put("size", 1313518);
		}
		res.writeJson(resp);
	}

}
