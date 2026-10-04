package com.bubblesessence.seguridad.auth;

import com.bubblesessence.common.exception.InvalidCredentialsException;
import com.bubblesessence.common.response.ApiResponse;
import com.bubblesessence.maestros.cliente.Cliente;
import com.bubblesessence.maestros.cliente.ClienteRepository;
import com.bubblesessence.seguridad.auth.dto.*;
import com.bubblesessence.seguridad.usuario.Usuario;
import com.bubblesessence.seguridad.usuario.UsuarioRepository;
import com.bubblesessence.ventas.pedido.Pedido;
import com.bubblesessence.ventas.pedido.PedidoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Autenticación para:
 * - Personal interno: admin, operador, vendedor, repartidor (endpoint /login)
 * - Clientes externos: comprador con cuenta (endpoints /registro-cliente, /login-cliente)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> new InvalidCredentialsException("Usuario o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new InvalidCredentialsException("Este usuario está inactivo");
        }
        if (!passwordEncoder.matches(request.getClave(), usuario.getClaveHash())) {
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario);

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .id(usuario.getId())
                .nombreCompleto(usuario.getNombreCompleto())
                .usuario(usuario.getUsuario())
                .rol(usuario.getRol())
                .build();

        return ApiResponse.success("Login exitoso", response);
    }

    @PostMapping("/registro-cliente")
    public ApiResponse<ClienteLoginResponseDTO> registroCliente(@Valid @RequestBody RegistroClienteRequestDTO request) {
        // Validar documento único
        if (clienteRepository.existsByDocumento(request.getDocumento())) {
            throw new InvalidCredentialsException("El documento ya está registrado");
        }

        // Validar email único
        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new InvalidCredentialsException("El email ya está registrado");
        }

        // Crear cliente
        Cliente cliente = Cliente.builder()
                .documento(request.getDocumento())
                .nombreCompleto(request.getNombreCompleto())
                .correo(request.getCorreo())
                .claveHash(passwordEncoder.encode(request.getClave()))
                .telefono(request.getTelefono())
                .activo(true)
                .build();

        Cliente clienteGuardado = clienteRepository.save(cliente);

        // VINCULAR AUTOMÁTICAMENTE pedidos invitados por documento
        List<Pedido> pedidosInvitados = pedidoRepository.findByInvitadoDocumentoAndClienteIsNull(request.getDocumento());
        for (Pedido pedido : pedidosInvitados) {
            pedido.setCliente(clienteGuardado);
            pedidoRepository.save(pedido);
        }

        // Generar token JWT
        String token = jwtService.generarTokenCliente(clienteGuardado);

        ClienteLoginResponseDTO response = ClienteLoginResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .id(clienteGuardado.getId())
                .nombreCompleto(clienteGuardado.getNombreCompleto())
                .documento(clienteGuardado.getDocumento())
                .correo(clienteGuardado.getCorreo())
                .telefono(clienteGuardado.getTelefono())
                .build();

        return ApiResponse.success("Registro exitoso", response);
    }

    @PostMapping("/login-cliente")
    public ApiResponse<ClienteLoginResponseDTO> loginCliente(@Valid @RequestBody LoginClienteRequestDTO request) {
        Cliente cliente = clienteRepository.findByDocumento(request.getDocumento())
                .orElseThrow(() -> new InvalidCredentialsException("Documento o contraseña incorrectos"));

        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new InvalidCredentialsException("Esta cuenta está inactiva");
        }

        if (!passwordEncoder.matches(request.getClave(), cliente.getClaveHash())) {
            throw new InvalidCredentialsException("Documento o contraseña incorrectos");
        }

        String token = jwtService.generarTokenCliente(cliente);

        ClienteLoginResponseDTO response = ClienteLoginResponseDTO.builder()
                .token(token)
                .tipoToken("Bearer")
                .id(cliente.getId())
                .nombreCompleto(cliente.getNombreCompleto())
                .documento(cliente.getDocumento())
                .correo(cliente.getCorreo())
                .telefono(cliente.getTelefono())
                .build();

        return ApiResponse.success("Login exitoso", response);
    }
}
