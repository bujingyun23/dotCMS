package com.dotcms.rest;

import com.dotcms.publisher.bundle.bean.Bundle;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.DotStateException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.dotmarketing.util.json.JSONArray;
import com.dotmarketing.util.json.JSONException;
import com.dotmarketing.util.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import com.dotcms.repackage.commons_lang_2_4.org.apache.commons.lang.StringEscapeUtils;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.GET;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.Path;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.PathParam;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.Produces;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.core.CacheControl;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.core.Context;
import com.dotcms.repackage.jersey_1_12.javax.ws.rs.core.Response;

import java.net.URISyntaxException;
import java.util.List;


@Path("/bundle")
public class BundleResource extends WebResource {

    /**
     * Returns a list of un-send bundles (haven't been sent to any Environment) filtered by owner and name
     *
     * @param request
     * @param params
     * @return
     * @throws DotStateException
     * @throws DotDataException
     * @throws DotSecurityException
     * @throws JSONException
     */
    @GET
    @Path ("/getunsendbundles/{params:.*}")
    @Produces ("application/json")
    public Response getUnsendBundles ( @Context HttpServletRequest request, @PathParam ("params") String params ) throws DotStateException, DotDataException, DotSecurityException, JSONException {

        InitDataObject initData = init( params, true, request, true );
        //Creating an utility response object
        ResourceResponse responseResource = new ResourceResponse( initData.getParamsMap() );

        //Reading the parameters
        String userId = initData.getParamsMap().get( "userid" );
        String bundleName = request.getParameter( "name" );
        String startParam = request.getParameter( "start" );
        String countParam = request.getParameter( "count" );

        int start = 0;
        if ( UtilMethods.isSet( startParam ) ) {
            start = Integer.valueOf( startParam );
        }

        int offset = -1;
        if ( UtilMethods.isSet( countParam ) ) {
            offset = Integer.valueOf( countParam );
        }

        if ( UtilMethods.isSet( bundleName ) ) {
            if ( bundleName.equals( "*" ) ) {
                bundleName = null;
            } else {
                bundleName = bundleName.replaceAll( "\\*", "" );
            }
        }

        JSONArray jsonBundles = new JSONArray();

        //Find the unsend bundles
        List<Bundle> bundles;
        if ( bundleName == null ) {
            //Find all the bundles for this user
            bundles = APILocator.getBundleAPI().getUnsendBundles( userId, offset, start );
        } else {
            //Filter by name
            bundles = APILocator.getBundleAPI().getUnsendBundlesByName( userId, bundleName, offset, start );
        }
        for ( Bundle b : bundles ) {

            JSONObject jsonBundle = new JSONObject();
            jsonBundle.put( "id", b.getId() );
            jsonBundle.put( "name", StringEscapeUtils.unescapeJava(b.getName()));
            //Added to the response list
            jsonBundles.add( jsonBundle );
        }

        //Prepare the response
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put( "identifier", "id" );
        jsonResponse.put( "label", "name" );
        jsonResponse.put( "items", jsonBundles.toArray() );
        jsonResponse.put( "numRows", bundles.size() );

        CacheControl nocache=new CacheControl();
        nocache.setNoCache(true);
        return Response.ok(jsonResponse.toString()).cacheControl(nocache).build();
    }

	@GET
	@Path("/updatebundle/{params:.*}")
	@Produces("application/json")
	public Response updateBundle(@Context HttpServletRequest request, @PathParam("params") String params) throws IOException {
	
		InitDataObject initData = init(params, true, request, true);
	    //Creating an utility response object
	    ResourceResponse responseResource = new ResourceResponse( initData.getParamsMap() );
	
		String bundleId = initData.getParamsMap().get("bundleid");
		String bundleName = URLDecoder.decode(request.getParameter("bundleName"), "UTF-8");
		try {
	
			if(!UtilMethods.isSet(bundleId)) {
	            return responseResource.response( "false" );
			}
	
			Bundle bundle = APILocator.getBundleAPI().getBundleById(bundleId);
			bundle.setName(bundleName);
			APILocator.getBundleAPI().updateBundle(bundle);
	
		} catch (DotDataException e) {
			Logger.error(getClass(), "Error trying to update Bundle. Bundle ID: " + bundleId);
	        return responseResource.response( "false" );
		}
	
	    return responseResource.response("true");
	}

	@GET
	@Path("/deletepushhistory/{params:.*}")
	@Produces("application/json")
	public Response deletePushHistory(@Context HttpServletRequest request, @PathParam("params") String params) {

        InitDataObject initData = init(params, true, request, true);
        //Creating an utility response object
        ResourceResponse responseResource = new ResourceResponse( initData.getParamsMap() );

        String assetId = initData.getParamsMap().get("assetid");

		try {

			if(!UtilMethods.isSet(assetId)) {
                return responseResource.response( "false" );
			}

			APILocator.getPushedAssetsAPI().deletePushedAssets(assetId);

		} catch (DotDataException e) {
			Logger.error(getClass(), "Error trying to delete Pushed Assets for asset Id: " + assetId);
            return responseResource.response( "false" );
		}

        return responseResource.response( "true" );
	}

	@GET
	@Path("/deleteenvironmentpushhistory/{params:.*}")
	@Produces("application/json")
	public Response deleteEnvironmentPushHistory(@Context HttpServletRequest request, @PathParam("params") String params) {

        InitDataObject initData = init(params, true, request, true);
        //Creating an utility response object
        ResourceResponse responseResource = new ResourceResponse( initData.getParamsMap() );

		String environmentId = initData.getParamsMap().get("environmentid");

		try {

			if(!UtilMethods.isSet(environmentId)) {
                return responseResource.response( "false" );
			}

			APILocator.getPushedAssetsAPI().deletePushedAssetsByEnvironment(environmentId);

		} catch (DotDataException e) {
			Logger.error(getClass(), "Error trying to delete Pushed Assets for environment Id: " + environmentId);
            return responseResource.response( "false" );
		}

        return responseResource.response( "true" );
	}

}
