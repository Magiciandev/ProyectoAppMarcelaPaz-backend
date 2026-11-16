package com.mpaz.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mpaz.auth.dto.LoginRequest;
import com.mpaz.auth.dto.LoginResponse;
import com.mpaz.auth.dto.UsuarioDTO;
import com.mpaz.auth.exception.ResourceNotFoundException;
import com.mpaz.auth.exception.UnauthorizedException;
import com.mpaz.auth.model.Usuario;
import com.mpaz.auth.repository.UsuarioRepository;

@Service
public class AuthService {

    private static final String MENSAJE_CREDENCIALES = "Usuario o PIN incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /** Mismo mensaje si el usuario no existe, está inactivo o el PIN no coincide (no revela cuál falló). */
    public LoginResponse login(LoginRequest request) {
        String nombreUsuario = request.getUsuario().trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByUsuario(nombreUsuario)
                .filter(Usuario::isActivo)
                .filter(u -> passwordEncoder.matches(request.getPin(), u.getPinHash()))
                .orElseThrow(() -> new UnauthorizedException(MENSAJE_CREDENCIALES));

        JwtService.TokenGenerado generado = jwtService.generar(usuario);
        return LoginResponse.builder()
                .token(generado.token())
                .tipo("Bearer")
                .expiraEn(generado.expiraEn().toString())
                .usuarioId(usuario.getId())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol())
                .build();
    }

    public UsuarioDTO obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioDTO::fromModel)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
