package com.hornetimports.pedido;

import com.hornetimports.pedido.dto.*;
import com.hornetimports.user.Profile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping("/confirmar")
    public ResponseEntity<ConfirmarPedidoResponse> confirmar(
            @Valid @RequestBody ConfirmarPedidoRequest request,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(pedidoService.confirmarPedido(request, profile));
    }

    @GetMapping
    public ResponseEntity<Page<PedidoDTO>> getPedidos(
            @AuthenticationPrincipal Profile profile,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(pedidoService.getPedidosUsuario(profile, pageable));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoDTO> getPedido(
            @PathVariable String pedidoId,
            @AuthenticationPrincipal Profile profile) {
        return ResponseEntity.ok(pedidoService.getPedidoPorId(pedidoId, profile));
    }
}
