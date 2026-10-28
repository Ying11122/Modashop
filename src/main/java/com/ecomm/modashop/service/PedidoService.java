package com.ecomm.modashop.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecomm.modashop.dto.PedidoLineaResponse;
import com.ecomm.modashop.dto.PedidoRequest;
import com.ecomm.modashop.dto.PedidoResponse;
import com.ecomm.modashop.model.CarritoItem;
import com.ecomm.modashop.model.EstadoPedido;
import com.ecomm.modashop.model.Pedido;
import com.ecomm.modashop.model.PedidoLinea;
import com.ecomm.modashop.model.StockVariante;
import com.ecomm.modashop.repository.CarritoItemRepository;
import com.ecomm.modashop.repository.PedidoRepository;
import com.ecomm.modashop.repository.StockVarianteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepo;
    private final CarritoItemRepository carritoRepo;
    private final StockVarianteRepository stockRepo;

    // ---------- POST (público): convertir el carrito en pedido ----------

    @Transactional
    public PedidoResponse crear(String carritoId, PedidoRequest req) {
        List<CarritoItem> items = carritoRepo.findByCarritoIdOrderById(carritoId);
        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito está vacío");
        }

        // Siempre se bloquean las filas en el mismo orden: así dos pedidos simultáneos
        // no pueden quedarse esperándose el uno al otro (interbloqueo)
        List<CarritoItem> ordenados = items.stream()
                .sorted(Comparator
                        .comparing((CarritoItem i) -> i.getProducto().getId())
                        .thenComparing(i -> i.getTalla().getId()))
                .toList();

        Pedido pedido = new Pedido();
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setNombre(req.nombre().trim());
        pedido.setEmail(req.email().trim());
        pedido.setTelefono(vacioANulo(req.telefono()));
        pedido.setNotas(vacioANulo(req.notas()));

        for (CarritoItem item : ordenados) {
            String nombre = item.getProducto().getNombre();
            String talla = item.getTalla().getNombre();

            // Fila de stock bloqueada y leída con el valor más reciente
            StockVariante stock = stockRepo
                    .findForUpdate(item.getProducto().getId(), item.getTalla().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "«" + nombre + "» ya no está disponible en la talla " + talla));

            if (stock.getUnidades() < item.getCantidad()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo quedan " + stock.getUnidades() + " unidades de «" + nombre
                                + "» en la talla " + talla);
            }

            stock.setUnidades(stock.getUnidades() - item.getCantidad());

            PedidoLinea linea = new PedidoLinea();
            linea.setPedido(pedido);
            linea.setProductoId(item.getProducto().getId());
            linea.setTallaId(item.getTalla().getId());
            linea.setNombreProducto(nombre);
            linea.setColor(item.getProducto().getColor());
            linea.setTalla(talla);
            linea.setCantidad(item.getCantidad());
            pedido.getLineas().add(linea);
        }

        pedidoRepo.save(pedido);
        carritoRepo.deleteByCarritoId(carritoId);

        // Si algo falla antes de llegar aquí, la transacción se deshace entera:
        // ni se descuenta stock ni se guarda el pedido
        return toResponse(pedido);
    }

    // ---------- Almacén (admin) ----------

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(EstadoPedido estado) {
        List<Pedido> pedidos = (estado == null)
                ? pedidoRepo.findAllByOrderByIdDesc()
                : pedidoRepo.findByEstadoOrderByIdDesc(estado);
        return pedidos.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public PedidoResponse cambiarEstado(Long id, EstadoPedido nuevo) {
        Pedido pedido = buscar(id);
        EstadoPedido actual = pedido.getEstado();

        if (actual == nuevo) {
            return toResponse(pedido);
        }
        if (actual == EstadoPedido.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un pedido cancelado no se puede reabrir");
        }
        if (nuevo == EstadoPedido.CANCELADO) {
            if (actual == EstadoPedido.ENVIADO) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Un pedido enviado no se puede cancelar");
            }
            devolverStock(pedido);
        }

        pedido.setEstado(nuevo);
        return toResponse(pedido);
    }

    // ---------- auxiliares ----------

    private Pedido buscar(Long id) {
        return pedidoRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    // Repone las unidades. Si el producto o la talla ya no existen, esa línea se omite.
    private void devolverStock(Pedido pedido) {
        pedido.getLineas().stream()
                .sorted(Comparator.comparing(PedidoLinea::getProductoId)
                        .thenComparing(PedidoLinea::getTallaId))
                .forEach(l -> stockRepo.findForUpdate(l.getProductoId(), l.getTallaId())
                        .ifPresent(s -> s.setUnidades(s.getUnidades() + l.getCantidad())));
    }

    private String vacioANulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private PedidoResponse toResponse(Pedido p) {
        return new PedidoResponse(
                p.getId(), p.getFecha(), p.getEstado().name(),
                p.getNombre(), p.getEmail(), p.getTelefono(), p.getNotas(),
                p.getLineas().stream()
                        .map(l -> new PedidoLineaResponse(
                                l.getProductoId(), l.getNombreProducto(),
                                l.getColor(), l.getTalla(), l.getCantidad()))
                        .toList());
    }
}