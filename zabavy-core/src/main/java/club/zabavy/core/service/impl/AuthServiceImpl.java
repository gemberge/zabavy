package club.zabavy.core.service.impl;

import club.zabavy.core.dao.CredentialDAO;
import club.zabavy.core.domain.Vendor;
import club.zabavy.core.domain.entity.Credential;
import club.zabavy.core.domain.entity.User;
import club.zabavy.core.domain.exceptions.CredentialDoesNotExistException;
import club.zabavy.core.domain.exceptions.InvalidAuthTokenException;
import club.zabavy.core.domain.exceptions.NotAuthenticatedUserException;
import club.zabavy.core.domain.exceptions.UnsupportedVendorException;
import club.zabavy.core.service.AuthService;
import club.zabavy.core.service.UserService;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

@Service
public class AuthServiceImpl implements AuthService {

	@Value("${app.url}") private String appUrl;
	@Value("${app.secret}") private String appSecret;
	@Value("${vk.app.id}") private String vkAppId;
	@Value("${fb.app.id}") private String fbAppId;
	@Value("${vk.app.secret}") private String vkAppSecret;
	@Value("${fb.app.secret}") private String fbAppSecret;

	@Autowired
	CredentialDAO credentialDAO;

	@Autowired
	UserService userService;

	public String getAuthLink(String vendor, String action) {
		if(vendor.equalsIgnoreCase("vk")) {
			StringBuilder builder = new StringBuilder();
			builder.append("https://oauth.vk.com/authorize?client_id=")
					.append(vkAppId)
					.append("&scope=&redirect_uri=")
					.append(appUrl)
					.append("api/")
					.append(action)
					.append("/vk&response_type=code&v=5.21")
					.toString();
			return builder.toString();
		} else if(vendor.equalsIgnoreCase("fb")) {
			StringBuilder builder = new StringBuilder();
			builder.append("https://www.facebook.com/dialog/oauth?client_id=")
					.append(fbAppId)
					.append("&redirect_uri=")
					.append(appUrl)
					.append("api/")
					.append(action)
					.append("/fb&response_type=code")
					.toString();
			return builder.toString();
		}
		throw new UnsupportedVendorException();
	}

	public String getToken(String vendor, String code, String action) throws IOException {
		if(vendor.equalsIgnoreCase("vk")) {
			StringBuilder builder = new StringBuilder();
			builder.append("https://oauth.vk.com/access_token?client_id=")
					.append(vkAppId)
					.append("&client_secret=")
					.append(vkAppSecret)
					.append("&code=")
					.append(code)
					.append("&redirect_uri=")
					.append(appUrl)
					.append("api/")
					.append(action)
					.append("/vk");
			BufferedReader reader = new BufferedReader(new InputStreamReader((new URL(builder.toString())).openStream()));
			String resp = reader.readLine();
			reader.close();
			return JsonPath.read(resp, "$.access_token").toString();
		} else if(vendor.equalsIgnoreCase("fb")) {
			StringBuilder builder = new StringBuilder();
			builder.append("https://graph.facebook.com/oauth/access_token?client_id=")
					.append(fbAppId)
					.append("&client_secret=")
					.append(fbAppSecret)
					.append("&code=")
					.append(code)
					.append("&redirect_uri=")
					.append(appUrl)
					.append("api/")
					.append(action)
					.append("/fb");
			BufferedReader reader = new BufferedReader(new InputStreamReader((new URL(builder.toString())).openStream()));
			String resp = reader.readLine();
			reader.close();
			return resp.substring(13, resp.indexOf("&expires="));
		}
		throw new UnsupportedVendorException();
	}

	public void login(String vendor, String code, HttpServletResponse response) throws IOException {
		String token = getToken(vendor, code, "login");
		long vendorUserId = getVendorUserId(vendor, token);
		Credential credential = credentialDAO.find(Vendor.valueOf(vendor.toUpperCase()), vendorUserId);
		if(credential == null) {
			throw new CredentialDoesNotExistException();
		} else {
			Cookie cookie = new Cookie("zabavy.auth", createAuthToken(credential.getUser().getId()));
			cookie.setHttpOnly(true);
			cookie.setMaxAge(2592000); // 30 days
			response.addCookie(cookie);
		}
	}

	public String createAuthToken(long userId) throws UnsupportedEncodingException {
		String authToken = DigestUtils.md5DigestAsHex((userId + appSecret).getBytes());
		authToken = DigestUtils.md5DigestAsHex((userId + authToken).getBytes());
		return userId + ">>" + authToken;
	}

	public boolean isAuthTokenValid(String token) {
		String values[] = token.split(">>");
		try {
			return values[1].equals(createAuthToken(Long.parseLong(values[0])));
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}

	public User getUserFromCookie(HttpServletRequest request) {
		Cookie cookie = WebUtils.getCookie(request, "zabavy.auth");
		if(cookie == null) {
			throw new NotAuthenticatedUserException();
		} else {
			if(isAuthTokenValid(cookie.getValue())) {
				return userService.findById(Long.parseLong(cookie.getValue().split(">>")[0]));
			} else {
				throw new InvalidAuthTokenException();
			}
		}
	}

	private long getVendorUserId(String vendor, String token) throws IOException {
		String url, resp;
		BufferedReader reader;
		if(vendor.equalsIgnoreCase("fb")) {
			url = "https://graph.facebook.com/me?access_token=" + token;
			reader = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));
			resp = reader.readLine();
			reader.close();
			return Long.parseLong(JsonPath.read(resp, "$.id").toString());
		} else if(vendor.equalsIgnoreCase("vk")) {
			url = "https://api.vk.com/method/users.get?access_token=" + token;
			reader = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));
			resp = reader.readLine();
			reader.close();
			return Long.parseLong(JsonPath.read(resp, "$.response[0].uid").toString());
		}

		throw new UnsupportedVendorException();
	}


}