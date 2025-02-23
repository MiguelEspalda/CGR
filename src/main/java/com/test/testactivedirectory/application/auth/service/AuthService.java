package com.test.testactivedirectory.application.auth.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.testactivedirectory.application.auth.dto.ActiveDirectoryUserDto;
import com.test.testactivedirectory.application.auth.dto.AuthRequestDto;
import com.test.testactivedirectory.application.auth.dto.AuthResponseDto;
import com.test.testactivedirectory.application.auth.mapper.AuthMapper;
import com.test.testactivedirectory.application.auth.mapper.UserMapper;
import com.test.testactivedirectory.application.auth.usecase.IAuthUseCase;
import com.test.testactivedirectory.application.user.usecase.UserUseCase;
import com.test.testactivedirectory.domain.models.ActiveDirectoryUserModel;
import com.test.testactivedirectory.domain.models.UserModel;
import com.test.testactivedirectory.domain.repository.IActiveDirectoryUserRepository;
import com.test.testactivedirectory.domain.repository.IUserRepository;
import com.test.testactivedirectory.infrastructure.security.Jwt.providers.JwtAuthenticationProvider;
import com.unboundid.ldap.sdk.SearchResultEntry;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthService implements IAuthUseCase {

    private final IUserRepository userRepository;
    private final IActiveDirectoryUserRepository activeDirectoryUserRepository;

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    @Override
    public Map<String, Object> signIn(AuthRequestDto userRequest, HttpServletRequest servletRequest)
            throws JsonProcessingException {

        Map<String, Object> response = new HashMap<>();

        try {

            UserModel userModel = userRepository.findBySAMAccountName(userRequest.getSAMAccountName());

            System.err.println("userModel: " + userModel);
            if (userModel != null && userModel.getPassword().equals(userRequest.getPassword())) {

                AuthResponseDto userDto = AuthMapper.INSTANCE.toAuthResponDto(userModel);

                String token = jwtAuthenticationProvider.createToken(userDto);

                userDto.setToken(token);

                response.put("user", userDto);
                response.put("message", "User authenticated successfully");
                response.put("statusCode", 200);
                response.put("status", "success");
                return response;

            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;

    }

    @Override
    public Map<String, Object> authWithLDAPActiveDirectory(AuthRequestDto userRequest,
            HttpServletRequest servletRequest)
            throws JsonProcessingException {

        Map<String, Object> response = new HashMap<>();

        try {

            Boolean isAccountValid = activeDirectoryUserRepository.checkAccount(
                    userRequest.getSAMAccountName(),
                    userRequest.getPassword());

            if (isAccountValid) {

                AuthResponseDto userRequestDto = AuthMapper.INSTANCE.toAuthResponDto(userRequest);

                String token = jwtAuthenticationProvider.createToken(userRequestDto);

                userRequestDto.setToken(token);

                response.put("user", userRequestDto);
                response.put("message", "User authenticated successfully");
                response.put("statusCode", 200);
                response.put("status", "success");
                return response;
            }

        } catch (Exception e) {
            // TODO: handle exception
            System.err.println("Error en la capa de aplicaciontion en service: " + e.getMessage());
        }
        return null;
    }

}
