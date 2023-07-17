package com.atuldevelop.app.ws.ui.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atuldevelop.app.ws.service.impl.UserServicesImpl;
import com.atuldevelop.app.ws.shared.dto.AddressDto;
import com.atuldevelop.app.ws.shared.dto.UserDto;
import com.atuldevelop.app.ws.ui.model.response.UserRest;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@InjectMocks
	UserController userController;
	
	@Mock
	UserServicesImpl userService;
	
	UserDto userDto;
	
	final String USER_ID = "bfhry47fhdjd7463gdh";
	
	@BeforeEach
	void setUp() throws Exception {
		
		userDto = new UserDto();
        userDto.setFirstName("Sergey");
        userDto.setLastName("Kargopolov");
        userDto.setEmail("test@test.com");
        userDto.setEmailVerificationStatus(Boolean.FALSE);
        userDto.setEmailVerificationToken(null);
        userDto.setUserId(USER_ID);
        userDto.setAddresses(getAddressesDto());
        userDto.setEncryptedPassword("xcf58tugh47");
		
	}

	@Test
	final void testGetUser() {
	    when(userService.getUserByUserId(anyString())).thenReturn(userDto);	
	    
	    UserRest userRest = userController.getUser(USER_ID);
	    
	    assertNotNull(userRest);
	    assertEquals(USER_ID, userRest.getUserId());
	    assertEquals(userDto.getFirstName(), userRest.getFirstName());
	    assertEquals(userDto.getLastName(), userRest.getLastName());
	    //assertTrue(userDto.getAddresses().size() == userRest.getAddresses().size());
	}
	
	
	private List<AddressDto> getAddressesDto() {
		AddressDto addressDto = new AddressDto();
		addressDto.setType("shipping");
		addressDto.setCity("Vancouver");
		addressDto.setCountry("Canada");
		addressDto.setPostalCode("ABC123");
		addressDto.setStreetName("123 Street name");

		AddressDto billingAddressDto = new AddressDto();
		billingAddressDto.setType("billling");
		billingAddressDto.setCity("Vancouver");
		billingAddressDto.setCountry("Canada");
		billingAddressDto.setPostalCode("ABC123");
		billingAddressDto.setStreetName("123 Street name");

		List<AddressDto> addresses = new ArrayList<>();
		addresses.add(addressDto);
		addresses.add(billingAddressDto);

		return addresses;

	}

}
