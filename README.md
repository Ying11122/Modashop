# Moda Shop — Demo

Demo de un catálogo de moda con filtros, carrito y registro de pedidos.
No se realizan compras ni cobros reales. Utiliza únicamente datos ficticios.

## Acceso

[Abrir la demo](https://modashop-yn8l.onrender.com)

No necesitas instalar nada ni crear una cuenta para probar el catálogo.
El alojamiento gratuito puede suspender la aplicación por inactividad;
la primera carga puede tardar alrededor de un minuto o más.
Abre la web unos minutos antes de presentarla.

## Recorrido de demostración

1. Mostrar los seis productos y sus imágenes.
2. Probar la búsqueda y los filtros por categoría o talla.
3. Elegir una talla y añadir un producto al carrito.
4. Cambiar cantidades o eliminar artículos.
5. Finalizar un pedido con nombre y email ficticios, por ejemplo
   `Persona Demo` y `demo@example.com`. No introduzcas datos personales reales.
6. Comprobar la confirmación y el descuento de stock.

Los pedidos de prueba se guardan en la base de datos y consumen stock.

## Consultar pedidos y stock

Estas direcciones muestran datos en formato JSON:

- [Estado de la API](https://modashop-yn8l.onrender.com/api/ping):
  debe devolver `{"estado":"ok"}`.
- [Productos y stock](https://modashop-yn8l.onrender.com/api/productos):
  muestra las unidades disponibles por producto y talla.
- [Pedidos — acceso de administrador](https://modashop-yn8l.onrender.com/api/admin/pedidos):
  muestra los datos del pedido y sus líneas, con los más recientes primero.
  Si el navegador solicita credenciales, utiliza el usuario `almacen` y la
  contraseña configurada en Render como `ADMIN_PASSWORD`.
  No publiques ni compartas esa contraseña.

Para demostrar el cambio de stock:

1. Anota las unidades de un producto y talla en `/api/productos`.
2. Realiza un pedido de una unidad de ese producto y talla.
3. Actualiza `/api/productos`: debería quedar una unidad menos.
4. Consulta `/api/admin/pedidos` para comprobar el pedido registrado.

El stock se descuenta al crear el pedido, cuyo estado inicial es `PENDIENTE`.
Añadir artículos al carrito no descuenta unidades. Se muestra el stock actual;
no existe un historial completo de movimientos de stock.

## Tecnología y límites

- Java y Spring Boot para la aplicación y la API.
- Frontend y backend desplegados juntos mediante Docker en Render.
- Base de datos MySQL en Aiven.
- Imágenes estáticas incluidas en el proyecto; la base de datos guarda sus rutas.
- Sin pagos reales ni pantalla visual de administración.
- Sujeto a los límites y condiciones de los planes gratuitos de los proveedores.

Para presentar la demo, comparte únicamente la URL pública de la web.
