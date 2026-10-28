let carrito = [];

// ---------- Leer (GET) ----------

async function cargarCarrito() {
    try {
        carrito = await api.carrito();
        pintarCarrito();
    } catch (e) {
        mostrarMensaje("No se pudo cargar el carrito: " + e.message, true);
    }
}

function pintarCarrito() {
    const unidades = carrito.reduce((suma, i) => suma + i.cantidad, 0);
    $("carrito-contador").textContent = unidades;
    $("btn-vaciar").disabled = carrito.length === 0;
    $("btn-finalizar").disabled = carrito.length === 0;

    const lista = $("carrito-lista");
    if (carrito.length === 0) {
        lista.innerHTML = `<p class="vacio">El carrito está vacío.</p>`;
        return;
    }
    lista.innerHTML = carrito.map(lineaCarrito).join("");
}

function lineaCarrito(i) {
    const imagen = i.imagenUrl
        ? `<img src="${esc(i.imagenUrl)}" alt="">`
        : `<div class="mini-sin-imagen"></div>`;

    const aviso = i.disponible < i.cantidad
        ? `<p class="aviso">Ahora solo quedan ${i.disponible} uds. en esta talla</p>`
        : "";

    return `
        <div class="linea">
            ${imagen}
            <div class="linea-info">
                <strong>${esc(i.nombre)}</strong>
                <span>${esc(i.color)} · Talla ${esc(i.talla)}</span>
                ${aviso}
            </div>
            <div class="cantidad">
                <button class="btn" data-carrito="menos" data-id="${i.id}"
                        ${i.cantidad <= 1 ? "disabled" : ""}>−</button>
                <span>${i.cantidad}</span>
                <button class="btn" data-carrito="mas" data-id="${i.id}"
                        ${i.cantidad >= i.disponible ? "disabled" : ""}>+</button>
            </div>
            <button class="btn btn-peligro" data-carrito="borrar" data-id="${i.id}"
                    title="Quitar del carrito">✕</button>
        </div>`;
}

// ---------- Añadir (POST) ----------

async function anadirAlCarrito(productoId, tallaId) {
    try {
        await api.anadirAlCarrito({ productoId, tallaId, cantidad: 1 });
        await cargarCarrito();
        mostrarMensaje("Añadido al carrito");
    } catch (e) {
        mostrarMensaje(e.message, true);
    }
}

// ---------- Errores dentro de los diálogos ----------

function errorCarrito(texto) {
    const p = $("carrito-error");
    p.textContent = texto;
    p.hidden = !texto;
}

function errorPedido(texto) {
    const p = $("pedido-error");
    p.textContent = texto;
    p.hidden = !texto;
}

// ---------- Arranque: eventos del carrito y del pedido ----------

function iniciarCarrito() {
    $("btn-carrito").addEventListener("click", () => {
        errorCarrito("");
        $("dlg-carrito").showModal();
    });

    $("btn-cerrar-carrito").addEventListener("click", () => $("dlg-carrito").close());

    // Vaciar (DELETE de todo el carrito)
    $("btn-vaciar").addEventListener("click", async () => {
        if (!confirm("¿Vaciar el carrito?")) return;
        try {
            await api.vaciarCarrito();
            errorCarrito("");
            await cargarCarrito();
        } catch (e) {
            errorCarrito(e.message);
        }
    });

    // Botones de cada línea: −, +, ✕ (un solo listener para todos)
    $("carrito-lista").addEventListener("click", async (ev) => {
        const btn = ev.target.closest("button[data-carrito]");
        if (!btn) return;

        const id = Number(btn.dataset.id);
        const linea = carrito.find((i) => i.id === id);
        if (!linea) return;

        try {
            if (btn.dataset.carrito === "mas") {
                await api.actualizarCarrito(id, linea.cantidad + 1);    // PUT
            } else if (btn.dataset.carrito === "menos") {
                await api.actualizarCarrito(id, linea.cantidad - 1);    // PUT
            } else if (btn.dataset.carrito === "borrar") {
                await api.quitarDelCarrito(id);                         // DELETE
            }
            errorCarrito("");
            await cargarCarrito();
        } catch (e) {
            errorCarrito(e.message);
            await cargarCarrito();
        }
    });

    // ----- Finalizar pedido -----

    $("btn-finalizar").addEventListener("click", () => {
        $("dlg-carrito").close();
        errorPedido("");
        $("dlg-pedido").showModal();
    });

    $("btn-cancelar-pedido").addEventListener("click", () => $("dlg-pedido").close());

    $("form-pedido").addEventListener("submit", async (ev) => {
        ev.preventDefault();

        const botonEnviar = $("btn-enviar-pedido");
        botonEnviar.disabled = true; // evita enviar el pedido dos veces con un doble clic

        try {
            const pedido = await api.crearPedido({     // POST
                nombre: $("ped-nombre").value.trim(),
                email: $("ped-email").value.trim(),
                telefono: $("ped-telefono").value.trim(),
                notas: $("ped-notas").value.trim(),
            });

            $("form-pedido").reset();
            $("dlg-pedido").close();

            $("confirmacion-texto").textContent =
                `Hemos registrado tu pedido nº ${pedido.id}. Guarda este número como referencia.`;
            $("dlg-confirmacion").showModal();
        } catch (e) {
            errorPedido(e.message);
        } finally {
            botonEnviar.disabled = false;
            // Con éxito o con error, el stock o el carrito pueden haber cambiado
            await cargarCarrito();
            cargarProductos();
        }
    });

    $("btn-cerrar-confirmacion").addEventListener("click", () => $("dlg-confirmacion").close());

    cargarCarrito();
}