// =========================================================================
// Copyright 2018 T-Mobile, US
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// See the readme.txt file for additional language around disclaimer of warranties.
// =========================================================================

package com.tmobile.cso.vault.api.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.tmobile.cso.vault.api.controller.ControllerUtil;
import com.tmobile.cso.vault.api.exception.LogMessage;
import com.tmobile.cso.vault.api.model.AWSRole;
import com.tmobile.cso.vault.api.model.Safe;
import com.tmobile.cso.vault.api.model.SafeGroup;
import com.tmobile.cso.vault.api.model.SafeUser;
import com.tmobile.cso.vault.api.process.RequestProcessor;
import com.tmobile.cso.vault.api.process.Response;
import com.tmobile.cso.vault.api.utils.JSONUtil;
import com.tmobile.cso.vault.api.utils.ThreadLocalContext;

@Component
public class  SafesService {

	@Autowired
	private RequestProcessor reqProcessor;

	@Value("${vault.auth.method}")
	private String vaultAuthMethod;

	public static final String READ_POLICY="read";
	public static final String WRITE_POLICY="write";
	public static final String DENY_POLICY="deny";


	private static Logger log = LogManager.getLogger(SafesService.class);

	/**
	 * Get Folders
	 * @param token
	 * @param path
	 * @return
	 */
	public ResponseEntity<String> getFolders( String token, String path){
		String _path = "";
		if( "apps".equals(path)||"shared".equals(path)||"users".equals(path)){
			_path = "metadata/"+path;
		}else{
			_path = path;
		}
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Get Folders").
				put(LogMessage.MESSAGE, String.format ("Trying to get folders for [%s]", path)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		Response response = reqProcessor.process("/sdb/list","{\"path\":\""+_path+"\"}",token);
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Get Folders").
				put(LogMessage.MESSAGE, "Getting folders completed").
				put(LogMessage.STATUS, response.getHttpstatus().toString()).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
	}
	/**
	 * Get SDB Info
	 * @param token
	 * @param path
	 * @return
	 */
	public ResponseEntity<String> getInfo(String token, String path){
		String _path = "metadata/"+path;
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Get Info").
				put(LogMessage.MESSAGE, String.format ("Trying to get Info for [%s]", path)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		Response response = reqProcessor.process("/sdb","{\"path\":\""+_path+"\"}",token);
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Get Info").
				put(LogMessage.MESSAGE, "Getting Info completed").
				put(LogMessage.STATUS, response.getHttpstatus().toString()).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());

	}

	/**
	 * Create a folder
	 * @param token
	 * @param path
	 * @return
	 */
	public ResponseEntity<String> createfolder(String token, String path){

		path = (path != null) ? path.toLowerCase() : path;
		if(ControllerUtil.isPathValid(path)){
			//if(ControllerUtil.isValidSafe(path, token)){
			String jsonStr ="{\"path\":\""+path +"\",\"data\":{\"default\":\"default\"}}";
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Create Folder").
					put(LogMessage.MESSAGE, String.format ("Trying to Create folder [%s]", path)).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			Response response = reqProcessor.process("/sdb/createfolder",jsonStr,token);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)) {
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Create Folder").
						put(LogMessage.MESSAGE, "Create Folder completed").
						put(LogMessage.STATUS, response.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Folder created \"]}");
			}
			return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			//}else{
			//	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid safe\"]}");
			//}
		}else{
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Create Folder").
					put(LogMessage.MESSAGE, "Create Folder failed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid path\"]}");
		}

	}
	
	/**
	 * Creates Safe
	 * @param token
	 * @param safe
	 * @return
	 */
	public ResponseEntity<String> createSafe(String token, Safe safe) {
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Create SDB").
				put(LogMessage.MESSAGE, String.format ("Trying to Create SDB")).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		if (!ControllerUtil.areSDBInputsValid(safe)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid input values\"]}");
		}

		ControllerUtil.converSDBInputsToLowerCase(safe);
		String path = safe.getPath();
		String jsonStr = JSONUtil.getJSON(safe);
		Map<String,Object> rqstParams = ControllerUtil.parseJson(jsonStr);
		
		if(ControllerUtil.isValidSafePath(path)){
			Response response = reqProcessor.process("/sdb/create",jsonStr,token);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				/*
				 * Store the metadata. Create policies if folders are created under the mount points
				 * 
				 */
				String _path = "metadata/"+path;
				rqstParams.put("path",_path);

				String metadataJson = 	ControllerUtil.convetToJson(rqstParams);
				response = reqProcessor.process("/write",metadataJson,token);

				boolean isMetaDataUpdated = false;

				if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
					isMetaDataUpdated = true;
				}

				String folders[] = path.split("[/]+");
				if(folders.length==2){
					String Safe = folders[1];
					Map<String,Object> policyMap = new HashMap<String,Object>();
					Map<String,String> accessMap = new HashMap<String,String>();
					accessMap.put(path+"/*","read");

					policyMap.put("accessid", "r_"+folders[0]+"_"+Safe);
					policyMap.put("access", accessMap);

					String policyRequestJson = 	ControllerUtil.convetToJson(policyMap);

					Response r_response = reqProcessor.process("/access/update",policyRequestJson,token);
					//Write Policy
					accessMap.put(path+"/*","write");
					policyMap.put("accessid", "w_"+folders[0]+"_"+Safe);

					policyRequestJson = 	ControllerUtil.convetToJson(policyMap);
					Response w_response = reqProcessor.process("/access/update",policyRequestJson,token); 
					//deny Policy
					accessMap.put(path+"/*","deny");
					policyMap.put("accessid", "d_"+folders[0]+"_"+Safe);

					policyRequestJson = 	ControllerUtil.convetToJson(policyMap);
					Response d_response = reqProcessor.process("/access/update",policyRequestJson,token); 

					accessMap.put(path+"/*", "sudo");
					accessMap.put(_path+"/*", "sudo");
					policyMap.put("accessid", "s_"+folders[0]+"_"+Safe);
					policyRequestJson = ControllerUtil.convetToJson(policyMap);
					Response s_response = reqProcessor.process("/access/update",policyRequestJson,token);

					if( (r_response.getHttpstatus().equals(HttpStatus.NO_CONTENT) && 
							w_response.getHttpstatus().equals(HttpStatus.NO_CONTENT) &&
							d_response.getHttpstatus().equals(HttpStatus.NO_CONTENT) &&
							s_response.getHttpstatus().equals(HttpStatus.NO_CONTENT) 
							) ||
							(r_response.getHttpstatus().equals(HttpStatus.OK) && 
									w_response.getHttpstatus().equals(HttpStatus.OK) &&
									d_response.getHttpstatus().equals(HttpStatus.OK)) &&
							s_response.getHttpstatus().equals(HttpStatus.OK) 
						){
						
						log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Create SDB").
								put(LogMessage.MESSAGE, "SDB Create Success").
								put(LogMessage.STATUS, response.getHttpstatus().toString()).
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Safe and associated read/write/deny policies created \"]}");
					}else{
						log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Create SDB").
								put(LogMessage.MESSAGE, "SDB Create Success").
								put(LogMessage.STATUS, response.getHttpstatus().toString()).
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						return ResponseEntity.status(HttpStatus.MULTI_STATUS).body("{\"messages\":[\"Safe created however one ore more policy (read/write/deny) creation failed \"]}");
					}
				}
				if(isMetaDataUpdated) {
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Create SDB").
							put(LogMessage.MESSAGE, "SDB Create Success").
							put(LogMessage.STATUS, response.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Safe created \"]}");
				}
				else {
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Create SDB").
							put(LogMessage.MESSAGE, "SDB Create Success").
							put(LogMessage.STATUS, response.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Safe created however metadata update failed. Please try with Safe/update \"]}");
				}
			}else{
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Create SDB").
						put(LogMessage.MESSAGE, "SDB Create completed").
						put(LogMessage.STATUS, response.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}
		}else{
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Create SDB").
					put(LogMessage.MESSAGE, "SDB Creation failed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}
	/**
	 * Gets Safe
	 */
	public ResponseEntity<String> getSafe(String token, String path) {
		if (path != null && path.startsWith("/")) {
			path = path.substring(1, path.length());
		}
		if (path != null && path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		String _path = "metadata/"+path;
		if( "apps".equals(path)||"shared".equals(path)||"users".equals(path)){
			Response response = reqProcessor.process("/sdb/list","{\"path\":\""+_path+"\"}",token);
			return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
		}else{
			boolean isPathValid = ControllerUtil.isValidSafePath(path);
			
			if (!isPathValid) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
			}
			String safeName = ControllerUtil.getSafeName(path);
			if (StringUtils.isEmpty(safeName)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'Safe Name' specified\"]}");
			}
			Response response = reqProcessor.process("/sdb","{\"path\":\""+_path+"\"}",token);
			if (HttpStatus.OK.equals(response.getHttpstatus())) {
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}
			else {
				return ResponseEntity.status(response.getHttpstatus()).body("{\"errors\":[\"Unable to get Safe information\"]}");
			}
		}
	}

	/**
	 * Deletes Safe
	 * @param token
	 * @param safe
	 * @return
	 */
	public ResponseEntity<String> deleteSafe(String token, Safe safe) {
		String path = safe.getPath();
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Delete SDB").
				put(LogMessage.MESSAGE, String.format ("Trying to Delete SDB [%s]", path)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		if(ControllerUtil.isValidSafePath(path) && ControllerUtil.isValidSafe(path, token)){
			Response response = new Response(); 
			ControllerUtil.recursivedeletesdb("{\"path\":\""+path+"\"}",token,response);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				deletePolicies(token, safe);
				return deleteSafeTree(token, safe);

			}else{
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete SDB").
						put(LogMessage.MESSAGE, "SDB Deletion completed").
						put(LogMessage.STATUS, response.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}
		}else if(ControllerUtil.isValidDataPath(path)){
			Response response = new Response(); 
			ControllerUtil.recursivedeletesdb("{\"path\":\""+path+"\"}",token,response);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete SDB").
						put(LogMessage.MESSAGE, "SDB Deletion completed").
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Folder deleted\"]}");
			}else{
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete SDB").
						put(LogMessage.MESSAGE, "SDB Deletion completed").
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}

		}else{
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Delete SDB").
					put(LogMessage.MESSAGE, "SDB Deletion failed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}

	/**
	 * Updates Safe
	 * @param token
	 * @param safe
	 * @return
	 */
	public ResponseEntity<String>  updateSafe(String token, Safe safe) {
		Map<String, Object> requestParams = ControllerUtil.parseJson(JSONUtil.getJSON(safe));
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Update SDB").
				put(LogMessage.MESSAGE, String.format ("Trying to Update SDB ")).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		if (!ControllerUtil.areSDBInputsValidForUpdate(requestParams)) {
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Update SDB").
					put(LogMessage.MESSAGE, String.format ("Invalid input values ")).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid input values\"]}");
		}
		@SuppressWarnings("unchecked")
		Map<Object,Object> data = (Map<Object,Object>)requestParams.get("data");
		String path = safe.getPath();
		String safeName = safe.getSafeBasicDetails().getName();
		String safeNameFromPath = ControllerUtil.getSafeName(path);
		String safeType = ControllerUtil.getSafeType(path);
		
		int redundantSafeNamesCount = ControllerUtil.getCountOfSafesForGivenSafeName(safeName, token);
		if (redundantSafeNamesCount > 1) {
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Update SDB").
					put(LogMessage.MESSAGE, String.format ("Safe can't be updated since duplicate safe names are found")).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Safe can't be updated since duplicate safe names are found\"]}");
		}

		String safePath = ControllerUtil.generateSafePath(safeName, safeType);
		String _path = "metadata/"+safeType+"/"+safeNameFromPath; //Path as passed 
		String _safePath = "metadata/"+safeType+"/"+safeName; // Path created from given safename and type
		String pathToBeUpdated = _path;
		
		if(ControllerUtil.isValidSafePath(path) || ControllerUtil.isValidSafePath(safePath)){
			// Get Safe metadataInfo
			Response response = reqProcessor.process("/read","{\"path\":\""+_path+"\"}",token);
			Map<String, Object> responseMap = null;
			if(HttpStatus.OK.equals(response.getHttpstatus())) {
				responseMap = ControllerUtil.parseJson(response.getResponse());
				if(responseMap.isEmpty()) {
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Error Fetching existing safe info \"]}");
				}
				pathToBeUpdated = _path;
			}
			else{
				response = reqProcessor.process("/read","{\"path\":\""+_safePath+"\"}",token);
				if(HttpStatus.OK.equals(response.getHttpstatus())){
					responseMap = ControllerUtil.parseJson(response.getResponse());
					if(responseMap.isEmpty()) {
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Error Fetching existing safe info \"]}");
					}
					pathToBeUpdated = _safePath;
				}
				else {
					log.error("Could not fetch the safe information. Possible path issue");
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Error Fetching existing safe info. please check the path specified \"]}");
				}
			}

			@SuppressWarnings("unchecked")
			Map<String,Object> metadataMap = (Map<String,Object>)responseMap.get("data");
			Object awsroles = metadataMap.get("aws-roles");
			Object groups = metadataMap.get("groups");
			Object users = metadataMap.get("users");
			data.put("aws-roles",awsroles);
			data.put("groups",groups);
			data.put("users",users);
			requestParams.put("path",pathToBeUpdated);
			// Do not alter the name of the safe
			((Map<String,Object>)requestParams.get("data")).put("name",(String) metadataMap.get("name"));
			
			String metadataJson = ControllerUtil.convetToJson(requestParams) ;
			response = reqProcessor.process("/sdb/update",metadataJson,token);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Update SDB").
						put(LogMessage.MESSAGE, "SDB Update Success").
						put(LogMessage.STATUS, response.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Safe updated \"]}");
			}else{
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Update SDB").
						put(LogMessage.MESSAGE, "SDB Update completed").
						put(LogMessage.RESPONSE, response.getResponse()).
						put(LogMessage.STATUS, response.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}
		}else{
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Update SDB").
					put(LogMessage.MESSAGE, "SDB Update failed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}
	/**
	 * Deletes Safe Policies
	 * @param token
	 * @param safe
	 */
	private void deletePolicies (String token, Safe safe) {
		Response response = new Response(); 
		ControllerUtil.recursivedeletesdb("{\"path\":\""+safe.getPath()+"\"}",token,response);
		if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){

			String folders[] = safe.getPath().split("[/]+");
			String r_policy = "r_";
			String w_policy = "w_";
			String d_policy = "d_";
			if (folders.length > 0) {
				for (int index = 0; index < folders.length; index++) {
					if (index == folders.length -1 ) {
						r_policy += folders[index];
						w_policy += folders[index];
						d_policy += folders[index];
					}
					else {
						r_policy += folders[index]  +"_";
						w_policy += folders[index] +"_";
						d_policy += folders[index] +"_";
					}
				}
			}
			reqProcessor.process("/access/delete","{\"accessid\":\""+r_policy+"\"}",token);
			reqProcessor.process("/access/delete","{\"accessid\":\""+w_policy+"\"}",token);
			reqProcessor.process("/access/delete","{\"accessid\":\""+d_policy+"\"}",token);	
		}
	}

	/**
	 * Deletes Policies, Groups, Roles associated with Safe
	 * @param token
	 * @param safe
	 * @return
	 */
	private ResponseEntity<String> deleteSafeTree(String token, Safe safe) {
		String path = safe.getPath();
		String _path = "metadata/"+path;

		// Get Safe metadataInfo
		Response response = reqProcessor.process("/sdb","{\"path\":\""+_path+"\"}",token);
		Map<String, Object> responseMap = null;
		try {
			responseMap = new ObjectMapper().readValue(response.getResponse(), new TypeReference<Map<String, Object>>(){});
		} catch (IOException e) {
			log.error(e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Error Fetching existing safe info \"]}");
		}
		if(responseMap!=null && responseMap.get("data")!=null){
			Map<String,Object> metadataMap = (Map<String,Object>)responseMap.get("data");
			Map<String,String> awsroles = (Map<String, String>)metadataMap.get("aws-roles");
			Map<String,String> groups = (Map<String, String>)metadataMap.get("groups");
			Map<String,String> users = (Map<String, String>) metadataMap.get("users");
			ControllerUtil.updateUserPolicyAssociationOnSDBDelete(path,users,token);
			ControllerUtil.updateGroupPolicyAssociationOnSDBDelete(path,groups,token);
			ControllerUtil.deleteAwsRoleOnSDBDelete(path,awsroles,token);
		}	
		ControllerUtil.recursivedeletesdb("{\"path\":\""+_path+"\"}",token,response);
		return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Safe deleted\"]}");


	}

	/**
	 * Adds user to a group
	 * @param token
	 * @param safeUser
	 * @return
	 */
	public ResponseEntity<String> addUserToSafe(String token, SafeUser safeUser) {

		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Add User to SDB").
				put(LogMessage.MESSAGE, String.format ("Trying to add user to SDB folder ")).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		
		if(!ControllerUtil.areSafeUserInputsValid(safeUser)) {
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Add User to SDB").
					put(LogMessage.MESSAGE, String.format ("Invalid user inputs [%s]", safeUser.toString())).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid input values\"]}");
		}
		
		String userName = safeUser.getUsername();
		String path = safeUser.getPath();
		String access = safeUser.getAccess();
		
		userName = (userName !=null) ? userName.toLowerCase() : userName;
		access = (access != null) ? access.toLowerCase(): access;

		boolean canAddUser = ControllerUtil.canAddPermission(path, token);
		if(canAddUser){

			String folders[] = path.split("[/]+");

			String policyPrefix ="";
			switch (access){
			case "read": policyPrefix = "r_"; break ; 
			case "write": policyPrefix = "w_" ;break; 
			case "deny": policyPrefix = "d_" ;break; 
			case "sudo": policyPrefix = "s_" ;break; 
			}
			if("".equals(policyPrefix)){
				log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Add User to SDB").
						put(LogMessage.MESSAGE, String.format ("Incorrect access requested. Valid values are read,write,deny")).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("{\"errors\":[\"Incorrect access requested. Valid values are read,write,deny \"]}");
			}

			String policy = policyPrefix+folders[0].toLowerCase()+"_"+folders[1];
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Add User to SDB").
					put(LogMessage.MESSAGE, String.format ("policy is [%s]", policy)).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			String r_policy = "r_";
			String w_policy = "w_";
			String d_policy = "d_";
			String s_policy = "s_";
			if (folders.length > 0) {
				for (int index = 0; index < folders.length; index++) {
					if (index == folders.length -1 ) {
						r_policy += folders[index];
						w_policy += folders[index];
						d_policy += folders[index];
						s_policy += folders[index];
					}
					else {
						r_policy += folders[index]  +"_";
						w_policy += folders[index] +"_";
						d_policy += folders[index] +"_";
						s_policy += folders[index] +"_";
					}
				}
			}
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Add User to SDB").
					put(LogMessage.MESSAGE, String.format ("Policies are, read - [%s], write - [%s], deny -[%s]", r_policy, w_policy, d_policy)).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			Response userResponse;
			if ("userpass".equals(vaultAuthMethod)) {
				userResponse = reqProcessor.process("/auth/userpass/read","{\"username\":\""+userName+"\"}",token);	
			}
			else {
				userResponse = reqProcessor.process("/auth/ldap/users","{\"username\":\""+userName+"\"}",token);
			}
			
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Add User to SDB").
					put(LogMessage.MESSAGE, String.format ("userResponse status is [%s]", userResponse.getHttpstatus())).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			
			String responseJson="";


			String policies ="";
			String groups="";
			String currentpolicies ="";

			if(HttpStatus.OK.equals(userResponse.getHttpstatus())){
				responseJson = userResponse.getResponse();	
				try {
					ObjectMapper objMapper = new ObjectMapper();
					currentpolicies = ControllerUtil.getPoliciesAsStringFromJson(objMapper, responseJson);
					if (!("userpass".equals(vaultAuthMethod))) {
						groups =objMapper.readTree(responseJson).get("data").get("groups").asText();
					}
				} catch (IOException e) {
					log.error(e);
					log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Add User to SDB").
							put(LogMessage.MESSAGE, String.format ("Exception while creating currentpolicies or groups")).
							put(LogMessage.STACKTRACE, e.getStackTrace().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
				}
				policies = currentpolicies;
				policies = policies.replaceAll(r_policy, "");
				policies = policies.replaceAll(w_policy, "");
				policies = policies.replaceAll(d_policy, "");
				policies = policies.replaceAll(s_policy, "");
				policies = policies+","+policy;
			}else{
				// New user to be configured
				policies = policy;
			}
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Add User to SDB").
					put(LogMessage.MESSAGE, String.format ("policies [%s] before calling configureUserpassUser/configureLDAPUser", policies)).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			Response ldapConfigresponse;
			if ("userpass".equals(vaultAuthMethod)) {
				ldapConfigresponse = ControllerUtil.configureUserpassUser(userName,policies,token);
			}
			else {
				ldapConfigresponse = ControllerUtil.configureLDAPUser(userName,policies,groups,token);
			}

			if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){ 
				Map<String,String> params = new HashMap<String,String>();
				params.put("type", "users");
				params.put("name",userName);
				params.put("path",path);
				params.put("access",access);
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Add User to SDB").
						put(LogMessage.MESSAGE, String.format ("Trying to update metadata [%s]", params.toString())).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				Response metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"User is successfully associated \"]}");		
				}else{
					String safeType = ControllerUtil.getSafeType(path);
					String safeName = ControllerUtil.getSafeName(path);
					List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
					String newPath = path;
					if (safeNames != null ) {
						
						for (String existingSafeName: safeNames) {
							if (existingSafeName.equalsIgnoreCase(safeName)) {
								// It will come here when there is only one valid safe
								newPath = safeType + "/" + existingSafeName;
								break;
							}
						} 
						
					}
					params.put("path",newPath);
					metadataResponse = ControllerUtil.updateMetadata(params,token);
					if (metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())) {
						return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"User is successfully associated \"]}");		
					}
					else {
						log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Add User to SDB").
								put(LogMessage.MESSAGE, "User configuration failed. Trying to revert...").
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						if ("userpass".equals(vaultAuthMethod)) {

							ldapConfigresponse = ControllerUtil.configureUserpassUser(userName,currentpolicies,token);
						}
						else {
							ldapConfigresponse = ControllerUtil.configureLDAPUser(userName,currentpolicies,groups,token);
						}
						if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
							log.debug("Reverting user policy uupdate");
							log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
									put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
									put(LogMessage.ACTION, "Add User to SDB").
									put(LogMessage.MESSAGE, "User configuration failed. Trying to revert...Passed").
									put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
									build()));
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Please try again\"]}");
						}else{
							log.debug("Reverting user policy update failed");
							log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
									put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
									put(LogMessage.ACTION, "Add User to SDB").
									put(LogMessage.MESSAGE, "User configuration failed. Trying to revert...failed").
									put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
									build()));
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Contact Admin \"]}");
						}
					}
				}		
			}else{
				log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Add User to SDB").
						put(LogMessage.MESSAGE, String.format ("Trying to configureUserpassUser/configureLDAPUser failed")).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Try Again\"]}");
			}	
		}else{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}

	/**
	 * Adds group to an Safe
	 * @param token
	 * @param safeGroup
	 * @return
	 */
	public ResponseEntity<String> addGroupToSafe(String token, SafeGroup safeGroup) {
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Add Group to SDB").
				put(LogMessage.MESSAGE, String.format ("Trying to add Group to SDB folder")).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		if(!ControllerUtil.areSafeGroupInputsValid(safeGroup)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid input values\"]}");
		}
		String jsonstr = JSONUtil.getJSON(safeGroup);
		if ("userpass".equals(vaultAuthMethod)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":\"This operation is not supported for Userpass authentication. \"}");
		}	
		ObjectMapper objMapper = new ObjectMapper();
		Map<String,String> requestMap = null;
		try {
			requestMap = objMapper.readValue(jsonstr, new TypeReference<Map<String,String>>() {});
		} catch (IOException e) {
			log.error(e);
		}

		String groupName = requestMap.get("groupname");
		String path = requestMap.get("path");
		String access = requestMap.get("access");
		groupName = (groupName !=null) ? groupName.toLowerCase() : groupName;
		path = (path != null) ? path.toLowerCase() : path;
		access = (access != null) ? access.toLowerCase(): access;

		boolean canAddGroup = ControllerUtil.canAddPermission(path, token);
		if(canAddGroup){
			String folders[] = path.split("[/]+");

			String policyPrefix ="";
			switch (access){
			case "read": policyPrefix = "r_"; break ; 
			case "write": policyPrefix = "w_" ;break; 
			case "deny": policyPrefix = "d_" ;break; 
			}
			if("".equals(policyPrefix)){
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("{\"errors\":[\"Incorrect access requested. Valid values are read,write,deny \"]}");
			}
			String policy = policyPrefix+folders[0]+"_"+folders[1];
			String r_policy = "r_";
			String w_policy = "w_";
			String d_policy = "d_";
			if (folders.length > 0) {
				for (int index = 0; index < folders.length; index++) {
					if (index == folders.length -1 ) {
						r_policy += folders[index];
						w_policy += folders[index];
						d_policy += folders[index];
					}
					else {
						r_policy += folders[index]  +"_";
						w_policy += folders[index] +"_";
						d_policy += folders[index] +"_";
					}
				}
			}
			Response getGrpResp = reqProcessor.process("/auth/ldap/groups","{\"groupname\":\""+groupName+"\"}",token);
			String responseJson="";

			String policies ="";
			String currentpolicies ="";

			if(HttpStatus.OK.equals(getGrpResp.getHttpstatus())){
				responseJson = getGrpResp.getResponse();	
				try {
					currentpolicies = ControllerUtil.getPoliciesAsStringFromJson(objMapper, responseJson);
				} catch (IOException e) {
					log.error(e);
				}
				policies = currentpolicies;
				policies = policies.replaceAll(r_policy, "");
				policies = policies.replaceAll(w_policy, "");
				policies = policies.replaceAll(d_policy, "");
				policies = policies+","+policy;
			}else{
				// New user to be configured
				policies = policy;
			}

			Response ldapConfigresponse = ControllerUtil.configureLDAPGroup(groupName,policies,token);

			if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				Map<String,String> params = new HashMap<String,String>();
				params.put("type", "groups");
				params.put("name",groupName);
				params.put("path",path);
				params.put("access",access);
				Response metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(metadataResponse !=null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Add Group to SDB").
							put(LogMessage.MESSAGE, "Group configuration Success.").
							put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Group is successfully associated with Safe\"]}");		
				}else{
					String safeType = ControllerUtil.getSafeType(path);
					String safeName = ControllerUtil.getSafeName(path);
					List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
					String newPath = path;
					if (safeNames != null ) {
						
						for (String existingSafeName: safeNames) {
							if (existingSafeName.equalsIgnoreCase(safeName)) {
								// It will come here when there is only one valid safe
								newPath = safeType + "/" + existingSafeName;
								break;
							}
						} 
						
					}
					params.put("path",newPath);
					metadataResponse = ControllerUtil.updateMetadata(params,token);
					if (metadataResponse !=null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())) {
						log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Add Group to SDB").
								put(LogMessage.MESSAGE, "Group configuration Success.").
								put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Group is successfully associated with Safe\"]}");		
						
					}
					else {
						log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Add Group to SDB").
								put(LogMessage.MESSAGE, "Group configuration failed.").
								put(LogMessage.RESPONSE, metadataResponse.getResponse()).
								put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						ldapConfigresponse = ControllerUtil.configureLDAPGroup(groupName,currentpolicies,token);
						if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
							log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
									put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
									put(LogMessage.ACTION, "Add Group to SDB").
									put(LogMessage.MESSAGE, "Reverting user policy update failed").
									put(LogMessage.RESPONSE, metadataResponse.getResponse()).
									put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
									put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
									build()));
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"erros\":[\"Group configuration failed.Please try again\"]}");
						}else{
							log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
									put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
									put(LogMessage.ACTION, "Add Group to SDB").
									put(LogMessage.MESSAGE, "Reverting user policy update failed").
									put(LogMessage.RESPONSE, metadataResponse.getResponse()).
									put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
									put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
									build()));
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Group configuration failed.Contact Admin \"]}");
						}
					}
				}		
			}else{
				ldapConfigresponse.getResponse();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Group configuration failed.Try Again\"]}");
			}	
		}else{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}
	/**
	 * Removes an associated user from Safe
	 * @param token
	 * @param safeUser
	 * @return
	 */
	public ResponseEntity<String> removeUserFromSafe(String token, SafeUser safeUser) {
		String jsonstr = JSONUtil.getJSON(safeUser);
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
			      put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				  put(LogMessage.ACTION, "Remove User from SDB").
			      put(LogMessage.MESSAGE, String.format ("Trying to remove user from SDB [%s]", jsonstr)).
			      put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
			      build()));
		ObjectMapper objMapper = new ObjectMapper();
		Map<String,String> requestMap = null;
		try {
			requestMap = objMapper.readValue(jsonstr, new TypeReference<Map<String,String>>() {});
		} catch (IOException e) {
			log.error(e);
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "removeUserFromSafe").
					put(LogMessage.MESSAGE, "Exception occurred while creating requestMap from input jsonstr").
					put(LogMessage.RESPONSE,e.getMessage()).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
		}

		String userName = requestMap.get("username");
		if (StringUtils.isEmpty(userName)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"username can't be empty\"]}");
		}
		String path = requestMap.get("path");
		boolean canDeletePermission = ControllerUtil.canAddPermission(path, token);
		if(ControllerUtil.isValidSafePath(path) && canDeletePermission){
			String folders[] = path.split("[/]+");

			String r_policy = "r_";
			String w_policy = "w_";
			String d_policy = "d_";
			String s_policy = "s_";
			if (folders.length > 0) {
				for (int index = 0; index < folders.length; index++) {
					if (index == folders.length -1 ) {
						r_policy += folders[index];
						w_policy += folders[index];
						d_policy += folders[index];
						s_policy += folders[index];
					}
					else {
						r_policy += folders[index] +"_";
						w_policy += folders[index] +"_";
						d_policy += folders[index] +"_";
						s_policy += folders[index] +"_";
					}
				}
			}
			Response userResponse;
			if ("userpass".equals(vaultAuthMethod)) {	
				userResponse = reqProcessor.process("/auth/userpass/read","{\"username\":\""+userName+"\"}",token);
			}
			else {
				userResponse = reqProcessor.process("/auth/ldap/users","{\"username\":\""+userName+"\"}",token);
			}
			String responseJson="";
			String policies ="";
			String groups="";
			String currentpolicies ="";

			if(HttpStatus.OK.equals(userResponse.getHttpstatus())){
				responseJson = userResponse.getResponse();	
				try {
					currentpolicies = ControllerUtil.getPoliciesAsStringFromJson(objMapper, responseJson);
					if (!("userpass".equals(vaultAuthMethod))) {
						groups =objMapper.readTree(responseJson).get("data").get("groups").asText();
					}
				} catch (IOException e) {
					log.error(e);
				}
				policies = currentpolicies;
				policies = policies.replaceAll(r_policy, "");
				policies = policies.replaceAll(w_policy, "");
				policies = policies.replaceAll(d_policy, "");
				policies = policies.replaceAll(s_policy, "");
				Response ldapConfigresponse;
				if ("userpass".equals(vaultAuthMethod)) {
					ldapConfigresponse = ControllerUtil.configureUserpassUser(userName,policies,token);
				}
				else {
					ldapConfigresponse = ControllerUtil.configureLDAPUser(userName,policies,groups,token);
				}
				if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
					Map<String,String> params = new HashMap<String,String>();
					params.put("type", "users");
					params.put("name",userName);
					params.put("path",path);
					params.put("access","delete");
					Response metadataResponse = ControllerUtil.updateMetadata(params,token);
					if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
						return ResponseEntity.status(HttpStatus.OK).body("{\"Message\":\"User association is removed \"}");		
					}else{
						String safeType = ControllerUtil.getSafeType(path);
						String safeName = ControllerUtil.getSafeName(path);
						List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
						String newPath = path;
						if (safeNames != null ) {
							
							for (String existingSafeName: safeNames) {
								if (existingSafeName.equalsIgnoreCase(safeName)) {
									// It will come here when there is only one valid safe
									newPath = safeType + "/" + existingSafeName;
									break;
								}
							} 
							
						}
						params.put("path",newPath);
						metadataResponse = ControllerUtil.updateMetadata(params,token);
						if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())) {
							return ResponseEntity.status(HttpStatus.OK).body("{\"Message\":\"User association is removed \"}");
						}
						else {
							log.debug("Meta data update failed");
							log.debug(metadataResponse.getResponse());
							if ("userpass".equals(vaultAuthMethod)) {
								ldapConfigresponse = ControllerUtil.configureUserpassUser(userName,currentpolicies,token);
							}
							else {
								ldapConfigresponse = ControllerUtil.configureLDAPUser(userName,currentpolicies,groups,token);
							}
							if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
								log.debug("Reverting user policy uupdate");
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Please try again\"]}");
							}else{
								log.debug("Reverting user policy update failed");
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Contact Admin \"]}");
							}
						}
					}		
				}else{
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Try Again\"]}");
				}	
			}else{
				// Trying to remove the orphan entries if exists
				Map<String,String> params = new HashMap<String,String>();
				params.put("type", "users");
				params.put("name",userName);
				params.put("path",path);
				params.put("access","delete");
				Response metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "removeUserFromSafe").
							put(LogMessage.MESSAGE, "Successfully removed of dangling user associations").
							put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
				}else{
					log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "removeUserFromSafe").
							put(LogMessage.MESSAGE, "Error occurred while removing of dangling user associations").
							put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
				}
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed. Please try again\"]}");
			}
		}else{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}
	/**
	 * Removes an associated group from LDAP
	 * @param token
	 * @param safeGroup
	 * @return
	 */
	public ResponseEntity<String> removeGroupFromSafe(String token, SafeGroup safeGroup) {
		String jsonstr = JSONUtil.getJSON(safeGroup);
		if ("userpass".equals(vaultAuthMethod)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":\"This operation is not supported for Userpass authentication. \"}");
		}	
		ObjectMapper objMapper = new ObjectMapper();
		Map<String,String> requestMap = null;
		try {
			requestMap = objMapper.readValue(jsonstr, new TypeReference<Map<String,String>>() {});
		} catch (IOException e) {
			log.error(e);
		}

		String groupName = requestMap.get("groupname");
		String path = requestMap.get("path");
		if(ControllerUtil.isValidSafePath(path) && ControllerUtil.isValidSafe(path, token)){
			String folders[] = path.split("[/]+");

			String r_policy = "r_";
			String w_policy = "w_";
			String d_policy = "d_";
			if (folders.length > 0) {
				for (int index = 0; index < folders.length; index++) {
					if (index == folders.length -1 ) {
						r_policy += folders[index];
						w_policy += folders[index];
						d_policy += folders[index];
					}
					else {
						r_policy += folders[index]  +"_";
						w_policy += folders[index] +"_";
						d_policy += folders[index] +"_";
					}
				}
			}
			Response userResponse = reqProcessor.process("/auth/ldap/groups","{\"groupname\":\""+groupName+"\"}",token);
			String responseJson="";
			String policies ="";
			String currentpolicies ="";

			if(HttpStatus.OK.equals(userResponse.getHttpstatus())){
				responseJson = userResponse.getResponse();	
				try {
					currentpolicies = ControllerUtil.getPoliciesAsStringFromJson(objMapper, responseJson);
				} catch (IOException e) {
					log.error(e);
				}
				policies = currentpolicies;
				policies = policies.replaceAll(r_policy, "");
				policies = policies.replaceAll(w_policy, "");
				policies = policies.replaceAll(d_policy, "");
				Response ldapConfigresponse = ControllerUtil.configureLDAPGroup(groupName,policies,token);
				if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){ 
					Map<String,String> params = new HashMap<String,String>();
					params.put("type", "groups");
					params.put("name",groupName);
					params.put("path",path);
					params.put("access","delete");
					Response metadataResponse = ControllerUtil.updateMetadata(params,token);
					if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
						return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Group association is removed \"]}");		
					}else{
						String safeType = ControllerUtil.getSafeType(path);
						String safeName = ControllerUtil.getSafeName(path);
						List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
						String newPath = path;
						if (safeNames != null ) {
							
							for (String existingSafeName: safeNames) {
								if (existingSafeName.equalsIgnoreCase(safeName)) {
									// It will come here when there is only one valid safe
									newPath = safeType + "/" + existingSafeName;
									break;
								}
							} 
							
						}
						params.put("path",newPath);
						metadataResponse = ControllerUtil.updateMetadata(params,token);
						if (metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())) {
							return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Group association is removed \"]}");	
						}
						else {
							log.debug("Meta data update failed");
							log.debug(metadataResponse.getResponse());
							ldapConfigresponse = ControllerUtil.configureLDAPGroup(groupName,currentpolicies,token);
							if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
								log.debug("Reverting user policy update");
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"Group configuration failed.Please try again\"]}");
							}else{
								log.debug("Reverting Group policy update failed");
								return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"Group configuration failed.Contact Admin \"]}");
							}
						}
					}		
				}else{
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"User configuration failed.Try Again\"]}");
				}	
			}else{
				// Trying to remove the orphan entries if exists
				Map<String,String> params = new HashMap<String,String>();
				params.put("type", "users");
				params.put("name",groupName);
				params.put("path",path);
				params.put("access","delete");
				Response metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					return ResponseEntity.status(HttpStatus.OK).body("{\"Message\":\"Group association is removed \"}");		
				}else{
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"messages\":[\"Group configuration failed.Try again \"]}");
				}
			}
		}else{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}
	/**
	 * Adds AWS Configuration to Safe
	 * @param token
	 * @param SafeawsConfiguration
	 * @return
	 */
	public ResponseEntity<String> addAwsRoleToSafe(String token, AWSRole awsRole) {
		String jsonstr = JSONUtil.getJSON(awsRole);
		ObjectMapper objMapper = new ObjectMapper();
		Map<String,String> requestMap = null;
		try {
			requestMap = objMapper.readValue(jsonstr, new TypeReference<Map<String,String>>() {});
		} catch (IOException e) {
			log.error(e);
		}
		if(!ControllerUtil.areAWSRoleInputsValid(requestMap)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid input values\"]}");
		}
		String role = (String)requestMap.get("role");
		String path = (String)requestMap.get("path");
		String access = (String)requestMap.get("access");

		role = (role !=null) ? role.toLowerCase() : role;
		path = (path != null) ? path.toLowerCase() : path;
		access = (access != null) ? access.toLowerCase(): access;

		boolean canAddAWSRole = ControllerUtil.canAddPermission(path, token);
		if(canAddAWSRole){
			String folders[] = path.split("[/]+");

			String policyPrefix ="";
			switch (access){
			case "read": policyPrefix = "r_"; break ; 
			case "write": policyPrefix = "w_" ;break; 
			case "deny": policyPrefix = "d_" ;break; 
			}
			if("".equals(policyPrefix)){
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("{\"errors\":[\"Incorrect access requested. Valid values are read,write,deny \"]}");
			}
			String policy = policyPrefix+folders[0]+"_"+folders[1];
			String r_policy = "r_";
			String w_policy = "w_";
			String d_policy = "d_";
			if (folders.length > 0) {
				for (int index = 0; index < folders.length; index++) {
					if (index == folders.length -1 ) {
						r_policy += folders[index];
						w_policy += folders[index];
						d_policy += folders[index];
					}
					else {
						r_policy += folders[index]  +"_";
						w_policy += folders[index] +"_";
						d_policy += folders[index] +"_";
					}
				}
			}
			Response roleResponse = reqProcessor.process("/auth/aws/roles","{\"role\":\""+role+"\"}",token);
			String responseJson="";
			String auth_type = "ec2";
			String policies ="";
			String currentpolicies ="";

			if(HttpStatus.OK.equals(roleResponse.getHttpstatus())){
				responseJson = roleResponse.getResponse();	
				try {
					JsonNode policiesArry =objMapper.readTree(responseJson).get("policies");
					for(JsonNode policyNode : policiesArry){
						currentpolicies =	(currentpolicies == "" ) ? currentpolicies+policyNode.asText():currentpolicies+","+policyNode.asText();
					}
					auth_type = objMapper.readTree(responseJson).get("auth_type").asText();
				} catch (IOException e) {
					log.error(e);
				}
				policies = currentpolicies;
				policies = policies.replaceAll(r_policy, "");
				policies = policies.replaceAll(w_policy, "");
				policies = policies.replaceAll(d_policy, "");
				policies = policies+","+policy;
			}else{
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("{\"errors\":[\"Non existing role name. Please configure it as first step\"]}");
			}
			Response ldapConfigresponse = null;
			if ("iam".equals(auth_type)) {
				ldapConfigresponse = ControllerUtil.configureAWSIAMRole(role,policies,token);
			}
			else {
				ldapConfigresponse = ControllerUtil.configureAWSRole(role,policies,token);
			}
			if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){ 
				Map<String,String> params = new HashMap<String,String>();
				params.put("type", "aws-roles");
				params.put("name",role);
				params.put("path",path);
				params.put("access",access);
				Response metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Role is successfully associated \"]}");		
				}else{
					String safeType = ControllerUtil.getSafeType(path);
					String safeName = ControllerUtil.getSafeName(path);
					List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
					String newPath = path;
					if (safeNames != null ) {
						
						for (String existingSafeName: safeNames) {
							if (existingSafeName.equalsIgnoreCase(safeName)) {
								// It will come here when there is only one valid safe
								newPath = safeType + "/" + existingSafeName;
								break;
							}
						} 
						
					}
					params.put("path",newPath);
					metadataResponse = ControllerUtil.updateMetadata(params,token);
					if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
						return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Role is successfully associated \"]}");	
					}
					else {
						System.out.println("Meta data update failed");
						System.out.println(metadataResponse.getResponse());
						ldapConfigresponse = ControllerUtil.configureAWSRole(role,policies,token);
						if(ldapConfigresponse.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
							System.out.println("Reverting user policy uupdate");
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Please try again\"]}");
						}else{
							System.out.println("Reverting user policy update failed");
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Contact Admin \"]}");
						}
					}
				}		
			}else{
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Try Again\"]}");
			}	
		}else{
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}
	
	/**
	 * Removes AWS role from Safe
	 * @param token
	 * @param awsRole
	 * @param detachOnly
	 * @return
	 */
	public ResponseEntity<String> removeAWSRoleFromSafe(String token, AWSRole awsRole, boolean detachOnly){
		String jsonstr = JSONUtil.getJSON(awsRole);
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
			      put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				  put(LogMessage.ACTION, "Delete AWS Role from Safe").
			      put(LogMessage.MESSAGE, String.format ("Trying to delete AWS Role from SDB [%s]", jsonstr)).
			      put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
			      build()));
		ObjectMapper objMapper = new ObjectMapper();
		Map<String,String> requestMap = null;
		try {
			requestMap = objMapper.readValue(jsonstr, new TypeReference<Map<String,String>>() {});
		} catch (IOException e) {
			log.error(e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid request. please check the request json\"]}");
		}
		
		String role = requestMap.get("role");
		String path = requestMap.get("path");
		if(ControllerUtil.isValidSafePath(path) && ControllerUtil.isValidSafe(path, token)){
			if (!detachOnly) { // delete mode, delete aws role as part of detachment of role from SDB.
				Response response = reqProcessor.process("/auth/aws/roles/delete","{\"role\":\""+role+"\"}",token);
				if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)) {
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Delete AWS Role from SDB").
							put(LogMessage.MESSAGE, "Delete AWS Role from SDB success").
							put(LogMessage.STATUS, response.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
					log.debug(role + " , AWS Role is deleted as part of detachment of role from SDB. Path " + path);
				} else {
					log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Delete AWS Role from SDB").
							put(LogMessage.MESSAGE, String.format("AWS Role deletion as part of sdb delete failed . SDB path [%s]", path)).
							put(LogMessage.RESPONSE, response.getResponse()).
							put(LogMessage.STATUS, response.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
					log.debug(role +" , AWS Role deletion as part of sdb delete failed . SDB path "+ path );
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Try Again\"]}");
				}
			}
			Map<String,String> params = new HashMap<>();
			params.put("type", "aws-roles");
			params.put("name",role);
			params.put("path",path);
			params.put("access","delete");
			Response metadataResponse = ControllerUtil.updateMetadata(params,token);
			if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						  put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						  put(LogMessage.ACTION, "Delete AWS Role from SDB").
						  put(LogMessage.MESSAGE, "Delete AWS Role from SDB success").
						  put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
						  put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						  build()));
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Role association is removed \"]}");
			}else{
				String safeType = ControllerUtil.getSafeType(path);
				String safeName = ControllerUtil.getSafeName(path);
				List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
				String newPath = path;
				if (safeNames != null ) {

					for (String existingSafeName: safeNames) {
						if (existingSafeName.equalsIgnoreCase(safeName)) {
							// It will come here when there is only one valid safe
							newPath = safeType + "/" + existingSafeName;
							break;
						}
					}

				}
				params.put("path",newPath);
				metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(metadataResponse !=null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							  put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							  put(LogMessage.ACTION, "Delete AWS Role from SDB").
							  put(LogMessage.MESSAGE, "Delete AWS Role from SDB success").
							  put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
							  put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							  build()));
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Role association is removed \"]}");
				}
				else {
					log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							  put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							  put(LogMessage.ACTION, "Delete AWS Role from SDB").
							  put(LogMessage.MESSAGE, "Delete AWS Role from SDB failed").
							  put(LogMessage.RESPONSE, metadataResponse.getResponse()).
							  put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
							  put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							  build()));
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Please try again\"]}");
				}
			}

		}else{
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				      put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					  put(LogMessage.ACTION, "Delete AWS Role from SDB").
				      put(LogMessage.MESSAGE, "Delete AWS Role from SDB failed").
				      put(LogMessage.RESPONSE, "Invalid Path").
				      put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				      build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}


	/**
	 * Delete a folder
	 * @param token
	 * @param path
	 * @return
	 */
	public ResponseEntity<String> deletefolder(String token, String path){
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Delete Folder").
				put(LogMessage.MESSAGE, String.format ("Trying to Delete folder [%s]", path)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		if(ControllerUtil.isPathValid(path) ){
			Response response = new Response();
			ControllerUtil.recursivedeletesdb("{\"path\":\""+path+"\"}",token,response);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				String folders[] = path.split("[/]+");
				String r_policy = "r_";
				String w_policy = "w_";
				String d_policy = "d_";

				if (folders.length > 0) {
					for (int index = 0; index < folders.length; index++) {
						if (index == folders.length -1 ) {
							r_policy += folders[index];
							w_policy += folders[index];
							d_policy += folders[index];
						}
						else {
							r_policy += folders[index]  +"_";
							w_policy += folders[index] +"_";
							d_policy += folders[index] +"_";
						}
					}
				}

				reqProcessor.process("/access/delete","{\"accessid\":\""+r_policy+"\"}",token);
				reqProcessor.process("/access/delete","{\"accessid\":\""+w_policy+"\"}",token);
				reqProcessor.process("/access/delete","{\"accessid\":\""+d_policy+"\"}",token);

				String _path = "metadata/"+path;

				// Get SDB metadataInfo
				response = reqProcessor.process("/sdb","{\"path\":\""+_path+"\"}",token);
				Map<String, Object> responseMap = null;
				try {
					responseMap = new ObjectMapper().readValue(response.getResponse(), new TypeReference<Map<String, Object>>(){});
				} catch (IOException e) {
					log.error(e);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Error Fetching existing safe info \"]}");
				}
				if(responseMap!=null && responseMap.get("data")!=null){
					Map<String,Object> metadataMap = (Map<String,Object>)responseMap.get("data");
					Map<String,String> awsroles = (Map<String, String>)metadataMap.get("aws-roles");
					Map<String,String> groups = (Map<String, String>)metadataMap.get("groups");
					Map<String,String> users = (Map<String, String>) metadataMap.get("users");
					ControllerUtil.updateUserPolicyAssociationOnSDBDelete(path,users,token);
					ControllerUtil.updateGroupPolicyAssociationOnSDBDelete(path,groups,token);
					ControllerUtil.deleteAwsRoleOnSDBDelete(path,awsroles,token);
				}
				ControllerUtil.recursivedeletesdb("{\"path\":\""+_path+"\"}",token,response);
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete Folder").
						put(LogMessage.MESSAGE, "SDB Folder Deletion completed").
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"SDB deleted\"]}");

			}else{
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete Folder ").
						put(LogMessage.MESSAGE, "SDB Folder Deletion Completed").
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}
		}else if(ControllerUtil.isValidDataPath(path)){
			Response response = new Response();
			ControllerUtil.recursivedeletesdb("{\"path\":\""+path+"\"}",token,response);
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
				log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete Folder").
						put(LogMessage.MESSAGE, "SDB Folder Deletion Completed").
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Folder deleted\"]}");
			}else{
				log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Delete Folder").
						put(LogMessage.MESSAGE, "SDB Folder Deletion Completed").
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
			}
		}else{
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Delete Folder").
					put(LogMessage.MESSAGE, "SDB Folder Deletion failed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid 'path' specified\"]}");
		}
	}

	/**
	 * Read from safe Recursively
	 * @param token
	 * @param path
	 * @return
	 */
	public ResponseEntity<String> getFoldersRecursively(String token, String path) {
		String _path = "";
		if( "apps".equals(path)||"shared".equals(path)||"users".equals(path)){
			_path = "metadata/"+path;
		}else{
			_path = path;
		}
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "getFoldersRecursively").
				put(LogMessage.MESSAGE, String.format ("Trying to get fodler recursively [%s]", path)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		Response response = reqProcessor.process("/sdb/list","{\"path\":\""+_path+"\"}",token);
		log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "getFoldersRecursively").
				put(LogMessage.MESSAGE, "getFoldersRecursively completed").
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
	}

	/**
	 * Create folder
	 * @param token
	 * @param path
	 * @return
	 */
	public ResponseEntity<String> createNestedfolder(String token, String path) {
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "createNestedfolder").
				put(LogMessage.MESSAGE, String.format ("Trying to createNestedfolder [%s]", path)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));
		path = (path != null) ? path.toLowerCase(): path;
		if(ControllerUtil.isPathValid(path)){
			String jsonStr ="{\"path\":\""+path +"\",\"data\":{\"default\":\"default\"}}";
			Response response = reqProcessor.process("/sdb/createfolder",jsonStr,token);
			log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "createNestedfolder").
					put(LogMessage.MESSAGE, "createNestedfolder completed").
					put(LogMessage.STATUS, response.getHttpstatus().toString()).
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			if(response.getHttpstatus().equals(HttpStatus.NO_CONTENT)) {
				return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Folder created \"]}");
			}
			return ResponseEntity.status(response.getHttpstatus()).body(response.getResponse());
		}else{
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "createNestedfolder").
					put(LogMessage.MESSAGE, "createNestedfolder completed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid path\"]}");
		}
	}

	/**
	 * Associate approle to Safe
	 * @param token
	 * @param jsonstr
	 * @return
	 */
	public ResponseEntity<String> associateApproletoSDB(String token, String jsonstr) {
		log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
				put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
				put(LogMessage.ACTION, "Associate AppRole to SDB").
				put(LogMessage.MESSAGE, String.format ("Trying to associate AppRole to SDB [%s]", jsonstr)).
				put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
				build()));

		Map<String,Object> requestMap = ControllerUtil.parseJson(jsonstr);
		if(!ControllerUtil.areSafeAppRoleInputsValid(requestMap)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"errors\":[\"Invalid input values\"]}");
		}
		String approle = requestMap.get("role_name").toString();
		String path = requestMap.get("path").toString();
		String access = requestMap.get("access").toString();

		boolean canAddAppRole = ControllerUtil.canAddPermission(path, token);
		if(canAddAppRole){

			log.info("Associate approle to SDB -  path :" + path + "valid" );

			String folders[] = path.split("[/]+");

			String policy ="";

			switch (access){
				case "read": policy = "r_" + folders[0].toLowerCase() + "_" + folders[1] ; break ;
				case "write": policy = "w_"  + folders[0].toLowerCase() + "_" + folders[1] ;break;
				case "deny": policy = "d_"  + folders[0].toLowerCase() + "_" + folders[1] ;break;
			}

			if("".equals(policy)){
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("{\"errors\":[\"Incorrect access requested. Valid values are read,write,deny \"]}");
			}


			log.info("Associate approle to SDB -  policy :" + policy + " is being configured" );

			//Call controller to update the policy for approle
			Response approleControllerResp = ControllerUtil.configureApprole(approle,policy,token);
			if(HttpStatus.OK.equals(approleControllerResp.getHttpstatus()) || (HttpStatus.NO_CONTENT.equals(approleControllerResp.getHttpstatus()))) {

				log.info("Associate approle to SDB -  policy :" + policy + " is associated" );
				log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Associate AppRole to SDB").
						put(LogMessage.MESSAGE, "Association of AppRole to SDB success").
						put(LogMessage.STATUS, approleControllerResp.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				Map<String,String> params = new HashMap<String,String>();
				params.put("type", "app-roles");
				params.put("name",approle);
				params.put("path",path);
				params.put("access",access);
				Response metadataResponse = ControllerUtil.updateMetadata(params,token);
				if(metadataResponse != null && HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
					log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
							put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
							put(LogMessage.ACTION, "Add AppRole To SDB").
							put(LogMessage.MESSAGE, "AppRole is successfully associated").
							put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
							put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
							build()));
					return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Approle :" + approle + " is successfully associated with SDB\"]}");
				}else{
					String safeType = ControllerUtil.getSafeType(path);
					String safeName = ControllerUtil.getSafeName(path);
					List<String> safeNames = ControllerUtil.getAllExistingSafeNames(safeType, token);
					String newPath = path;
					if (safeNames != null ) {

						for (String existingSafeName: safeNames) {
							if (existingSafeName.equalsIgnoreCase(safeName)) {
								// It will come here when there is only one valid safe
								newPath = safeType + "/" + existingSafeName;
								break;
							}
						}

					}
					params.put("path",newPath);
					metadataResponse = ControllerUtil.updateMetadata(params,token);
					if(HttpStatus.NO_CONTENT.equals(metadataResponse.getHttpstatus())){
						log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Add AppRole To SDB").
								put(LogMessage.MESSAGE, "AppRole is successfully associated").
								put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						return ResponseEntity.status(HttpStatus.OK).body("{\"messages\":[\"Approle :" + approle + " is successfully associated with SDB\"]}");
					}
					else {
						log.debug(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
								put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
								put(LogMessage.ACTION, "Add AppRole To SDB").
								put(LogMessage.MESSAGE, "AppRole configuration failed.").
								put(LogMessage.RESPONSE, metadataResponse.getResponse()).
								put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
								put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
								build()));
						//Trying to revert the metadata update in case of failure
						approleControllerResp = ControllerUtil.configureApprole(approle,policy,token);
						if(approleControllerResp.getHttpstatus().equals(HttpStatus.NO_CONTENT)){
							log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
									put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
									put(LogMessage.ACTION, "Add AppRole To SDB").
									put(LogMessage.MESSAGE, "Reverting user policy update failed").
									put(LogMessage.RESPONSE, metadataResponse.getResponse()).
									put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
									put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
									build()));
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Please try again\"]}");
						}else{
							log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
									put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
									put(LogMessage.ACTION, "Add AppRole To SDB").
									put(LogMessage.MESSAGE, "Reverting user policy update failed").
									put(LogMessage.RESPONSE, metadataResponse.getResponse()).
									put(LogMessage.STATUS, metadataResponse.getHttpstatus().toString()).
									put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
									build()));
							return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"errors\":[\"Role configuration failed.Contact Admin \"]}");
						}
					}
				}

			}else {
				log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
						put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
						put(LogMessage.ACTION, "Associate AppRole to SDB").
						put(LogMessage.MESSAGE, "Association of AppRole to SDB failed").
						put(LogMessage.RESPONSE, approleControllerResp.getResponse()).
						put(LogMessage.STATUS, approleControllerResp.getHttpstatus().toString()).
						put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
						build()));
				log.error( "Associate Approle" +approle + "to sdb FAILED");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"messages\":[\"Approle :" + approle + " failed to be associated with SDB\"]}");
			}
		} else {
			log.error(JSONUtil.getJSON(ImmutableMap.<String, String>builder().
					put(LogMessage.USER, ThreadLocalContext.getCurrentMap().get(LogMessage.USER).toString()).
					put(LogMessage.ACTION, "Associate AppRole to SDB").
					put(LogMessage.MESSAGE, "Association of AppRole to SDB failed").
					put(LogMessage.RESPONSE, "Invalid Path").
					put(LogMessage.APIURL, ThreadLocalContext.getCurrentMap().get(LogMessage.APIURL).toString()).
					build()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"messages\":[\"Approle :" + approle + " failed to be associated with SDB.. Invalid Path specified\"]}");
		}
	}
}
