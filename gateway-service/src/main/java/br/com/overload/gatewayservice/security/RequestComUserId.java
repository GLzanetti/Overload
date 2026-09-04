package br.com.overload.gatewayservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RequestComUserId extends HttpServletRequestWrapper {

    private final String userId;

    public RequestComUserId(HttpServletRequest request, String userId) {
        super(request);
        this.userId = userId;
    }

    @Override
    public String getHeader(String nome){
        if(nome.equals("X-User-Id")){
            return userId;
        }

        return super.getHeader(nome);
    }
}
