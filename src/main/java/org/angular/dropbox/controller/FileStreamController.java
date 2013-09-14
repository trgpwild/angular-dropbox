package org.angular.dropbox.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.stereotype.Component;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxEntry.File;
import com.dropbox.core.DbxThumbnailFormat;
import com.dropbox.core.DbxThumbnailSize;

@Component
@Path("/file")
public class FileStreamController {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{token}")
	public List<DbxEntry> list(@PathParam("token") String token) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			return listFiles(token);
		} catch (Exception e) {
			return new ArrayList<DbxEntry>();
		}
	}


	@GET
	@Produces("application/image")
	@Path("/{token}/{name}")
	public Response download(@PathParam("token") String token, @PathParam("name") String filename, @Context HttpServletResponse response) {
		try {
			setResponse(token, filename, response);
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.ok().build();
	}
	
	private List<DbxEntry> listFiles(String token) throws DbxException, IOException {
		DbxClient client = getClient(token);
		DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
		return listing.children;
	}


	private DbxClient getClient(String token) {
		DbxRequestConfig config = new DbxRequestConfig("angular-dropbox/1.0", Locale.getDefault().toString());
		DbxClient client = new DbxClient(config, token);
		return client;
	}

	private void setResponse(String token, String filename, HttpServletResponse response) throws DbxException, IOException {
		DbxClient client = getClient(token);
		for (DbxEntry child : listFiles(token)) {
			if (child.name.equals(filename) && child.isFile()) {
				File file = child.asFile();
				ServletOutputStream out = response.getOutputStream();
				DbxThumbnailSize size = new DbxThumbnailSize("m", 175, 240);
				client.getThumbnail(size, DbxThumbnailFormat.JPEG, file.path, file.rev, out);
				//client.getFile(file.path, file.rev, out);
			}
		}
	}

}