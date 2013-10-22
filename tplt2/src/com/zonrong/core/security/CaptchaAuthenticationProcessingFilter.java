package com.zonrong.core.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

public class CaptchaAuthenticationProcessingFilter extends UsernamePasswordAuthenticationFilter {

	public static final String SPRING_SECURITY_CAPTCHA_KEY = "_spring_security_captcha";
	private String captchaParameter = SPRING_SECURITY_CAPTCHA_KEY;
	private boolean captchaValid = true;
	private boolean captchaNoCase = true;

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException {

		if (captchaValid) {
			String reqCaptcha = StringUtils.trimAllWhitespace(request.getParameter(captchaParameter));
			String sesCaptcha = "";
			if (request.getSession(false) != null) {
				sesCaptcha = StringUtils.trimAllWhitespace((String) request.getSession().getAttribute(captchaParameter));
			}

			if (logger.isDebugEnabled()) {
				logger.debug("The request captcha is：" + reqCaptcha);
				logger.debug("The session captcha is：" + sesCaptcha);
			}

			if (captchaNoCase) {
				if(StringUtils.hasLength(reqCaptcha)){
					reqCaptcha = reqCaptcha.toLowerCase();
				}
				if(StringUtils.hasLength(sesCaptcha)){
					sesCaptcha = sesCaptcha.toLowerCase();
				}
			}

			if (!StringUtils.hasLength(sesCaptcha) || !StringUtils.hasLength(reqCaptcha) || !reqCaptcha.equals(sesCaptcha)) {
				throw new BadCredentialsException("captcha error");
			}

		}

		return super.attemptAuthentication(request, response);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException, ServletException {

		super.successfulAuthentication(request, response, authResult);
	}

	public String getCaptchaParameter() {
		return captchaParameter;
	}

	public void setCaptchaParameter(String captchaParameter) {
		this.captchaParameter = captchaParameter;
	}

	public boolean isCaptchaValid() {
		return captchaValid;
	}

	public void setCaptchaValid(boolean captchaValid) {
		this.captchaValid = captchaValid;
	}

	public boolean isCaptchaNoCase() {
		return captchaNoCase;
	}

	public void setCaptchaNoCase(boolean captchaNoCase) {
		this.captchaNoCase = captchaNoCase;
	}

}
