package com.zonrong.system.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * date: 2010-7-26
 *
 * version: 1.0
 * commonts: ......
 */
@Controller
public class LoginController {
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(LoginController.class);

	@RequestMapping(value="index", method = RequestMethod.GET)
	public ModelAndView showDesktop(HttpServletRequest request, HttpServletResponse response) {
	/*	try {
			request.getRequestDispatcher("/WEB-INF/index.html").forward(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}*/
		return new ModelAndView("index");
	}
}


