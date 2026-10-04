package com.bubblesessence.seguridad.usuario;

import com.bubblesessence.common.exception.BusinessException;
import com.bubblesessence.common.exception.ResourceNotFoundException;
import com.bubblesessence.seguridad.usuario.dto.UsuarioRequestDTO;
import com.bubblesessence.seguridad.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> listar(Boolean activo) {
        List<Usuario> usuarios = (activo != null)
                ? usuarioRepository.findByActivo(activo)
                : usuarioRepository.findAll();
        return usuarios.stream().map(usuarioMapper::toResponseDTO).toList();
    }

    @Override
    public UsuarioResponseDTO obtenerPorId(Long id) {
        return usuarioMapper.toResponseDTO(buscarEntidadOFallar(id));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        if (request.getClave() == null || request.getClave().isBlank()) {
            throw new BusinessException("La contraseña es obligatoria al crear un usuario");
        }
        if (usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new BusinessException("Ya existe un usuario con el login '%s'".formatted(request.getUsuario()));
        }
        if (request.getCorreo() != null && usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new BusinessException("Ya existe un usuario con el correo '%s'".formatted(request.getCorreo()));
        }

        Usuario usuario = Usuario.builder()
                .nombreCompleto(request.getNombreCompleto())
                .usuario(request.getUsuario())
                .claveHash(passwordEncoder.encode(request.getClave()))
                .rol(request.getRol())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .activo(request.getActivo() == null || request.getActivo())
                .build();

        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO request) {
        Usuario usuario = buscarEntidadOFallar(id);

        if (!usuario.getUsuario().equals(request.getUsuario())
                && usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new BusinessException("Ya existe un usuario con el login '%s'".formatted(request.getUsuario()));
        }

        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setUsuario(request.getUsuario());
        usuario.setRol(request.getRol());
        usuario.setTelefono(request.getTelefono());
        usuario.setCorreo(request.getCorreo());
        if (request.getActivo() != null) {
            usuario.setActivo(request.getActivo());
        }
        if (request.getClave() != null && !request.getClave().isBlank()) {
            usuario.setClaveHash(passwordEncoder.encode(request.getClave()));
        }

        return usuarioMapper.toResponseDTO(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarEntidadOFallar(id);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void activar(Long id) {
        Usuario usuario = buscarEntidadOFallar(id);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarEntidadOFallar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
