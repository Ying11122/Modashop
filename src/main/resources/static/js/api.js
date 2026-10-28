const API = "/api";

// ---------- Identificador del carrito (uno por navegador) ----------

function nuevoUuid() {
    if (crypto.randomUUID) return crypto.randomUUID();
    // Alternativa para contextos donde randomUUID no existe
    const b = crypto.getRandomValues(new Uint8Array(16));
    b[6] = (b[6] & 0x0f) | 0x40;
    b[8] = (b[8] & 0x3f) | 0x80;
    const h = [...b].map((x) => x.toString(16).padStart(2, "0")).join("");
    return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20)}`;
}

let idEnMemoria = null;

function carritoId() {
    try {
        let id = localStorage.getItem("carritoId");
        if (!id) {
            id = nuevoUuid();
            localStorage.setItem("carritoId", id);
        }
        return id;
    } catch (_) {
        // localStorage bloqueado (modo privado, etc.): el carrito dura mientras la pestaña esté abierta
        if (!idEnMemoria) idEnMemoria = nuevoUuid();
        return idEnMemoria;
    }
}

const cabeceraCarrito = () => ({ "X-Cart-Id": carritoId() });

// ---------- Función base ----------

async function request(url, options = {}) {
    const { headers, ...resto } = options;

    const res = await fetch(API + url, {
        ...resto,
        headers: { "Content-Type": "application/json", ...headers },
    });

    if (!res.ok) {
        let msg = `Error ${res.status}`;
        try {
            const body = await res.json();
            if (body.message) msg = body.message;
        } catch (_) {
            // la respuesta no traía JSON: nos quedamos con el mensaje genérico
        }
        throw new Error(msg);
    }

    // DELETE devuelve 204 sin cuerpo
    return res.status === 204 ? null : res.json();
}

const api = {
    // ----- Catálogo (solo lectura) -----
    categorias: () => request("/categorias"),
    tallas: () => request("/tallas"),
    etiquetas: () => request("/etiquetas"),

    productos: (filtros = {}) => {
        const params = new URLSearchParams();
        Object.entries(filtros).forEach(([clave, valor]) => {
            if (valor) params.append(clave, valor); // ignora vacíos
        });
        const qs = params.toString();
        return request("/productos" + (qs ? "?" + qs : ""));
    },

    producto: (id) => request(`/productos/${id}`),

    // ----- Carrito -----
    carrito: () => request("/carrito", { headers: cabeceraCarrito() }),

    anadirAlCarrito: (datos) =>
        request("/carrito", {
            method: "POST",
            headers: cabeceraCarrito(),
            body: JSON.stringify(datos),
        }),

    actualizarCarrito: (id, cantidad) =>
        request(`/carrito/${id}`, {
            method: "PUT",
            headers: cabeceraCarrito(),
            body: JSON.stringify({ cantidad }),
        }),

    quitarDelCarrito: (id) =>
        request(`/carrito/${id}`, { method: "DELETE", headers: cabeceraCarrito() }),

    vaciarCarrito: () =>
        request("/carrito", { method: "DELETE", headers: cabeceraCarrito() }),

       // ----- Pedidos -----
    crearPedido: (datos) =>
        request("/pedidos", {
            method: "POST",
            headers: cabeceraCarrito(),
            body: JSON.stringify(datos),
        }),
};