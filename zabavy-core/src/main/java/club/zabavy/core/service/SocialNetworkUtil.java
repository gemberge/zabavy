package club.zabavy.core.service;

import club.zabavy.core.domain.entity.User;
import club.zabavy.core.domain.exceptions.UnsupportedVendorException;
import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@Component
public class SocialNetworkUtil {

	@Value("${app.url}") private String appUrl;
	@Value("${vk.app.id}") private String vkAppId;
	@Value("${fb.app.id}") private String fbAppId;
	@Value("${vk.app.secret}") private String vkAppSecret;
	@Value("${fb.app.secret}") private String fbAppSecret;

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

	public long getVendorUserId(String vendor, String token) throws IOException {
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

	public User getVendorUserInfo(String vendor, String token) throws IOException {
		String url, resp, firstName, lastName, photoUrl;
		BufferedReader reader;
		if(vendor.equalsIgnoreCase("fb")) {
			url = "https://graph.facebook.com/me?fields=first_name,last_name,picture.height(200).width(200)&locale=uk_UK&access_token=" + token;
			reader = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));
			resp = reader.readLine();
			reader.close();
			firstName = JsonPath.read(resp, "$.first_name").toString();
			lastName = JsonPath.read(resp, "$.last_name").toString();
			photoUrl = JsonPath.read(resp, "$.picture.data.url").toString();
		} else if(vendor.equalsIgnoreCase("vk")) {
			url = "https://api.vk.com/method/users.get?fields=photo_200&access_token=" + token;
			URLConnection urlconn = (new URL(url)).openConnection();
			urlconn.setRequestProperty("Accept-Language", "uk-UK"); //without that VK returns translit
			reader = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
			resp = reader.readLine();
			reader.close();
			firstName = JsonPath.read(resp, "$.response[0].first_name").toString();
			lastName = JsonPath.read(resp, "$.response[0].last_name").toString();
			photoUrl = JsonPath.read(resp, "$.response[0].photo_200").toString();
		} else throw new UnsupportedVendorException();
		User user = new User();
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setPhotoUrl(photoUrl);
		return user;
	}

}
