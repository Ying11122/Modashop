// ---------- Referencias al DOM ----------
const $ = (id) => document.getElementById(id);

const catalogo = $("catalogo");
const mensaje = $("mensaje");

// ---------- Utilidades ----------

// Escapa texto antes de meterlo en HTML (evita inyección de código)
function esc(texto) {
    return String(texto ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

let temporizadorMensaje;
function mostrarMensaje(texto, esError = false) {
    mensaje.textContent = texto;
    mensaje.className = "mensaje " + (esError ? "error" : "ok");
    mensaje.hidden = false;
    clearTimeout(temporizadorMensaje);
    temporizadorMensaje = setTimeout(() => (mensaje.hidden = true), 4000);
}

function rellenarSelect(select, items, textoVacio) {
    select.innerHTML =
        `<option value="">${esc(textoVacio)}</option>` +
        items.map((i) => `<option value="${i.id}">${esc(i.nombre)}</option>`).join("");
}

// ---------- Orden de tallas ----------

const ORDEN_LETRAS = ["XXS", "XS", "S", "M", "L", "XL", "XXL", "XXXL"];
const EQUIVALENTES = { "2XS": "XXS", "2XL": "XXL", "3XL": "XXXL" };

function posicionTalla(nombre) {
    let t = String(nombre ?? "").trim().toUpperCase();
    t = EQUIVALENTES[t] ?? t;

    const i = ORDEN_LETRAS.indexOf(t);
    if (i !== -1) return i;                            // XS, S, M, L, XL...

    if (/^\d+([.,]\d+)?$/.test(t)) {
        return 100 + parseFloat(t.replace(",", "."));  // 36, 38, 40... tras las de letra
    }
    return 1000;                                       // cualquier otra, al final
}

// Para usar con sort(): ordena dos nombres de talla
const porTalla = (a, b) =>
    posicionTalla(a) - posicionTalla(b) || String(a).localeCompare(String(b), "es");

// ---------- Catálogo ----------

function filtrosActuales() {
    return {
        q: $("f-q").value.trim(),
        categoria: $("f-categoria").value,
        talla: $("f-talla").value,
        etiqueta: $("f-etiqueta").value,
    };
}

async function cargarProductos() {
    try {
        const productos = await api.productos(filtrosActuales());
        pintarCatalogo(productos);
    } catch (e) {
        mostrarMensaje("No se pudo cargar el catálogo: " + e.message, true);
    }
}

function pintarCatalogo(productos) {
    if (productos.length === 0) {
        catalogo.innerHTML = `<p class="vacio">No hay productos con esos filtros.</p>`;
        return;
    }
    catalogo.innerHTML = productos.map(tarjeta).join("");
}

// Cada tarjeta tiene SIEMPRE seis hijos directos, en este orden:
// imagen, título, descripción, etiquetas, tallas, compra.
// El CSS alinea esas seis filas entre todas las tarjetas.
function tarjeta(p) {
    const imagen = p.imagenUrl
        ? `<img src="${esc(p.imagenUrl)}" alt="${esc(p.nombre)}">`
        : `<div class="sin-imagen">Sin imagen</div>`;

    // El color va como primera etiqueta
    const etiquetasHtml = [
        p.color ? `<span class="chip chip-color">${esc(p.color)}</span>` : "",
        ...p.etiquetas.map((e) => `<span class="chip">${esc(e)}</span>`),
    ].join("");

    const stockOrdenado = [...p.stock].sort((a, b) => porTalla(a.talla, b.talla));

    const tallasHtml = stockOrdenado
        .map((s) => {
            const agotada = s.unidades === 0 ? " agotada" : "";
            return `<span class="talla${agotada}" title="${s.unidades} uds.">${esc(s.talla)}</span>`;
        })
        .join("");

    // Desplegable con todas las tallas en orden; las agotadas, desactivadas
    const hayStock = stockOrdenado.some((s) => s.unidades > 0);
    const compra = hayStock
        ? `<div class="compra">
               <select class="sel-talla" aria-label="Talla">
                   ${stockOrdenado
                       .map((s) => s.unidades > 0
                           ? `<option value="${s.tallaId}">${esc(s.talla)} (${s.unidades})</option>`
                           : `<option value="${s.tallaId}" disabled>${esc(s.talla)} (agotada)</option>`)
                       .join("")}
               </select>
               <button class="btn btn-primario" data-accion="anadir" data-id="${p.id}">
                   Añadir al carrito
               </button>
           </div>`
        : `<p class="agotado">Agotado</p>`;

    return `
        <article class="tarjeta">
            ${imagen}
            <div class="t-titulo">
                <span class="categoria">${esc(p.categoria)}</span>
                <h3>${esc(p.nombre)}</h3>
            </div>
            <p class="descripcion" title="${esc(p.descripcion)}">${esc(p.descripcion)}</p>
            <div class="chips">${etiquetasHtml}</div>
            <div class="tallas">${tallasHtml}</div>
            ${compra}
        </article>`;
}

// Un solo listener para los botones de las tarjetas (delegación de eventos)
catalogo.addEventListener("click", async (ev) => {
    const btn = ev.target.closest("button[data-accion='anadir']");
    if (!btn) return;

    const talla = btn.closest(".tarjeta").querySelector(".sel-talla").value;
    await anadirAlCarrito(Number(btn.dataset.id), Number(talla));
});

// ---------- Filtros ----------

let temporizadorBusqueda;
$("f-q").addEventListener("input", () => {
    clearTimeout(temporizadorBusqueda);
    temporizadorBusqueda = setTimeout(cargarProductos, 300); // espera a que dejes de teclear
});

["f-categoria", "f-talla", "f-etiqueta"].forEach((id) =>
    $(id).addEventListener("change", cargarProductos)
);

$("btn-limpiar").addEventListener("click", () => {
    $("f-q").value = "";
    $("f-categoria").value = "";
    $("f-talla").value = "";
    $("f-etiqueta").value = "";
    cargarProductos();
});

// ---------- Arranque ----------

async function iniciar() {
    let categorias, tallas, etiquetas;
    try {
        [categorias, tallas, etiquetas] = await Promise.all([
            api.categorias(),
            api.tallas(),
            api.etiquetas(),
        ]);
    } catch (e) {
        mostrarMensaje("No se pudo conectar con el servidor: " + e.message, true);
        return;
    }

    // El filtro de tallas también sale ordenado
    tallas.sort((a, b) => porTalla(a.nombre, b.nombre));

    rellenarSelect($("f-categoria"), categorias, "Todas las categorías");
    rellenarSelect($("f-talla"), tallas, "Todas las tallas");
    rellenarSelect($("f-etiqueta"), etiquetas, "Todas las etiquetas");

    cargarProductos();
    iniciarCarrito();
}

iniciar();