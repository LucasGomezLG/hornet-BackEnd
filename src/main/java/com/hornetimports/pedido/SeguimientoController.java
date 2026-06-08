package com.hornetimports.pedido;

import com.hornetimports.pedido.dto.SeguimientoPublicoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguimiento")
@RequiredArgsConstructor
public class SeguimientoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<SeguimientoPublicoDTO> seguimiento(
            @RequestParam String codigo) {
        return ResponseEntity.ok(pedidoService.getSeguimientoPublico(codigo));
    }
}
