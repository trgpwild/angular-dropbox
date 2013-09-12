package org.angular.dropbox.controller;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.stereotype.Component;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

@Component
@Path("/token")
public class TokenGeneratorController {

    @GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{key}/{secret}")
	public String url(@PathParam("key") String key, @PathParam("secret") String secret, @Context HttpServletRequest req) throws JsonGenerationException, JsonMappingException, IOException {
		DbxAppInfo appInfo = new DbxAppInfo(key, secret);
        DbxRequestConfig config = new DbxRequestConfig("angular-dropbox/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        String authorizeUrl = webAuth.start();
        HttpSession session = req.getSession(true);
        session.setAttribute("webAuth", webAuth);
        return authorizeUrl;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{code}")
	public String url(@PathParam("code") String code, @Context HttpServletRequest req) throws JsonGenerationException, JsonMappingException, IOException, DbxException {
        HttpSession session = req.getSession(true);
        DbxWebAuthNoRedirect webAuth = (DbxWebAuthNoRedirect) session.getAttribute("webAuth");
        code = code.trim();
        DbxAuthFinish authFinish;
        authFinish = webAuth.finish(code);
		return authFinish.accessToken;
	}

}