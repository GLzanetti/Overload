package br.com.overload.gatewayservice.router;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class GatewayController {

    private final RouterService routerService;

    public GatewayController(RouterService routerService) {
        this.routerService = routerService;
    }

    @RequestMapping("/**")
    public ResponseEntity<?> rotear(HttpServletRequest request) throws IOException {
        String metodo = request.getMethod();
        String path = request.getRequestURI();
        byte[] corpo = request.getInputStream().readAllBytes();
        String header = request.getHeader("Content-Type");

        return routerService.rotear(metodo, path, corpo, header);
    }
}
