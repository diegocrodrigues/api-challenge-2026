package com.cotefacil.gateway.service;

import com.cotefacil.gateway.dto.LoginRequest;
import com.cotefacil.gateway.dto.LoginResponse;

public interface IAuthService {

    LoginResponse login(LoginRequest request);
}
