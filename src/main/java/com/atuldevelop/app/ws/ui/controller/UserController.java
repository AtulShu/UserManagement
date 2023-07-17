package com.atuldevelop.app.ws.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atuldevelop.app.ws.exceptions.UserServiceException;
import com.atuldevelop.app.ws.service.AddressService;
import com.atuldevelop.app.ws.service.UserService;
import com.atuldevelop.app.ws.shared.Roles;
import com.atuldevelop.app.ws.shared.dto.AddressDto;
import com.atuldevelop.app.ws.shared.dto.UserDto;
import com.atuldevelop.app.ws.ui.model.request.PasswordResetModel;
import com.atuldevelop.app.ws.ui.model.request.PasswordResetRequestModel;
import com.atuldevelop.app.ws.ui.model.request.UserDetailsRequestModel;
import com.atuldevelop.app.ws.ui.model.response.AddressesRest;
import com.atuldevelop.app.ws.ui.model.response.ErrorMessages;
import com.atuldevelop.app.ws.ui.model.response.OperationStatusModel;
import com.atuldevelop.app.ws.ui.model.response.RequestOperationStatus;
import com.atuldevelop.app.ws.ui.model.response.UserRest;

import jakarta.servlet.http.HttpServletRequest;

@RestController
//@Secured("ROLE_ADMIN")
@RequestMapping("users")//http:localhost:8080/users
//@CrossOrigin(origins = "*")-> For all methods for any origin
public class UserController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	AddressService addressesService;
	
	@Autowired
	HttpServletRequest httpServletRequest;
	
//	@ApiOperation(value=" The Get User Details Web Service Endpoint",
//			notes="This web ........... For ex: ......")
//	Can be put in application properties file and used as notes="$()"
//	@ApiImplicitParams({
//	@ApiImplicitParam(name = "authorization", value = "${userController.authorizationHeader.description}", paramType = "header") })
	@PostAuthorize("hasRole('ADMIN') or returnObject.userId == principal.userId") //@PostAuthorize has access to return obejct in this case it is UserRest but we use returnObject 
	@GetMapping(path="/{id}", produces={ MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE})
	public UserRest getUser(@PathVariable String id) {
		UserRest returnValue  = new UserRest();
		UserDto userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, returnValue);
		return returnValue;
	}
	
	@PostMapping(consumes={ MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE},
				produces={ MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws Exception {
		if(userDetails.getFirstName() == null) {
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		}
		UserRest returnValue  = new UserRest();
//		UserDto userDto = new UserDto();
//		BeanUtils.copyProperties(userDetails, userDto);
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		userDto.setRoles(new HashSet<>(Arrays.asList(Roles.ROLE_USER.name())));
		UserDto createdUser = userService.createUser(userDto);
		returnValue = modelMapper.map(createdUser, UserRest.class); 
		return returnValue;
	}
	
	@PutMapping(path = "/{id}")
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest returnValue = new UserRest();

		UserDto userDto = new UserDto();
		userDto = new ModelMapper().map(userDetails, UserDto.class);

		UserDto updateUser = userService.updateUser(id, userDto);
		returnValue = new ModelMapper().map(updateUser, UserRest.class);

		return returnValue;
	}
	
	
	//@PreAuthorize("hasRole('ROLE_ADMIN') or #id == principal.userId") #id == principal.userId"-> if the logged in user is deleting it's own account
	//@PreAuthorize("hasAuthority('DELETE_AUTHORITY')")
	//@Secured("ROLE_ADMIN")
	@DeleteMapping(path = "/{id}")
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.DELETE.name());

		userService.deleteUser(id);

		returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return returnValue;
	}
	
	@GetMapping
	public List<UserRest> getUsers(@RequestParam(value="page", defaultValue="0") int page,
			@RequestParam(value="limit", defaultValue="15") int limit){
		List<UserRest> returnValue = new ArrayList<>();
		List<UserDto> users = userService.getUsers(page, limit);
		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}
		return returnValue;
	}
	
	@GetMapping(path = "/{id}/addresses", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE})
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> returnValue = new ArrayList<>();

		List<AddressDto> addressesDTO = addressesService.getAddresses(id);

		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			Type listType = new TypeToken<List<AddressesRest>>() {
			}.getType();
			returnValue = new ModelMapper().map(addressesDTO, listType);
			
			for (AddressesRest addressRest : returnValue) {
				Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
						.getUserAddress(id, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(selfLink);
			}
			
		}
		
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
				.getUserAddresses(id))
				.withSelfRel();
		return CollectionModel.of(returnValue, userLink, selfLink);
	}
	
	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE})
	public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId) {

		AddressDto addressesDto = addressesService.getAddress(addressId);
		ModelMapper modelMapper = new ModelMapper();
		AddressesRest returnValue = modelMapper.map(addressesDto, AddressesRest.class);
		//domainName:port/users/userId
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		Link userAddressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
				.getUserAddresses(userId)).
				withRel("addresses");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class).
				getUserAddress(userId, addressId))
				.withSelfRel();
//		returnValue.add(userLink);
//		returnValue.add(userAddressesLink);
//		returnValue.add(selfLink);
		
		return EntityModel.of(returnValue, Arrays.asList(userLink,userAddressesLink,selfLink));
	}
	
	@GetMapping(path = "/email_verification", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE})
	//@CrossOrigin(origins = "*")-> For just this method with all origins
	public OperationStatusModel verifyEmailToken(@RequestParam(value="token") String token) {
		OperationStatusModel returnValue = new OperationStatusModel();
		returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		boolean isVerified = userService.verifyEmailToken(token);
		if(isVerified) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		} else {
			returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		}
		
		return returnValue;
	}
	
	@GetMapping(path = "/password-reset-request", consumes={ MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE},
			produces={ MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public OperationStatusModel  requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
		OperationStatusModel returnValue = new OperationStatusModel();
		boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
		returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		if(operationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return returnValue;
	}
	
	@PostMapping(path = "/password-reset", consumes={ MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE},
			produces={ MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
		OperationStatusModel returnValue = new OperationStatusModel();
		boolean operationResult = userService.resetPassword(passwordResetModel.getToken(), passwordResetModel.getToken());
		returnValue.setOperationName(RequestOperationName.PASSWORD_RESET.name());
		returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
		if(operationResult) {
			returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return returnValue;
	}
}
