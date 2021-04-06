package org.integratedmodelling.klab.hub.partners.controllers;

import org.integratedmodelling.klab.api.API;
import org.integratedmodelling.klab.hub.api.MongoNode;
import org.integratedmodelling.klab.hub.api.MongoPartner;
import org.integratedmodelling.klab.hub.exception.BadRequestException;
import org.integratedmodelling.klab.hub.nodes.services.NodeService;
import org.integratedmodelling.klab.hub.partners.services.PartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.minidev.json.JSONObject;

@RestController
@RequestMapping(API.HUB.PARTNER_BASE)
public class MongoPartnersController {
	
	private PartnerService service;
	
	@Autowired
	MongoPartnersController(PartnerService service) {
		this.service = service;
	}
	
	@GetMapping(value = "", produces = "application/json")
	@PreAuthorize("hasRole('ROLE_SYSTEM') or hasRole('ROLE_ADMINSTRATOR')")
	public ResponseEntity<?> getAll() {
		JSONObject resp = new JSONObject();
		resp.appendField("Partners", service.getAll());
		return new ResponseEntity<>(resp, HttpStatus.OK);
	}
	
	@PutMapping(value = "/{name}", produces = "application/json")
	@PreAuthorize("hasRole('ROLE_SYSTEM')")
	public ResponseEntity<Object> update(@PathVariable("name") String name, @RequestBody MongoPartner partner) {
		if(name.equals(partner.getName())) {
			service.update(partner);	
		} else {
			throw new BadRequestException("Node name does not match url");
		}
		return new ResponseEntity<>("The Partner has been updated successsfully", HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/{name}", produces = "application/json")
	@PreAuthorize("hasRole('ROLE_SYSTEM')")
	public ResponseEntity<Object> delete(@PathVariable("name") String name) {
		service.delete(name);
		return new ResponseEntity<>("The Partner has been deleted successsfully", HttpStatus.OK);
	}
	
	@GetMapping(value= "/{name}", produces = "application/json")
	@PreAuthorize("hasRole('ROLE_SYSTEM') or hasRole('ROLE_ADMINISTRATOR')")
	public ResponseEntity<Object> getByName(@PathVariable("name") String name) {
		JSONObject resp = new JSONObject();
		resp.appendField("node", service.getByName(name));
		return new ResponseEntity<>(resp, HttpStatus.OK);		
	}
	
	@PostMapping(value="", produces = "application/json")
	@PreAuthorize("hasRole('ROLE_SYSTEM')")
	public ResponseEntity<Object> create(@RequestBody MongoPartner partner) {
	    partner = service.create(partner);
		return new ResponseEntity<>(partner, HttpStatus.CREATED);
	}

}
