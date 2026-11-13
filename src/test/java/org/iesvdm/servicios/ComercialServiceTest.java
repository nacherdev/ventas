package org.iesvdm.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.iesvdm.dao.ComercialDAO;
import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.modelo.PedidoDetalle;
import org.junit.jupiter.api.Test;

class ComercialServiceTest {

	@Test
	void calculaEstadisticasDePedidos() {
		ComercialService service = new ComercialService(
				mock(ComercialDAO.class),
				mock(PedidoDAO.class));
		List<PedidoDetalle> pedidos = List.of(
				new PedidoDetalle(1, 10.0, null, "Ana", "Luis", 1),
				new PedidoDetalle(2, 30.0, null, "Luis", "Luis", 2),
				new PedidoDetalle(3, 20.0, null, "Eva", "Luis", 3));

		var estadisticas = service.estadisticasPedidos(pedidos);

		assertEquals(3, estadisticas.getNumeroPedidos());
		assertEquals(60.0, estadisticas.getImporteTotal());
		assertEquals(20.0, estadisticas.getImporteMedio());
		assertEquals(30.0, estadisticas.getImporteMaximo());
		assertEquals(10.0, estadisticas.getImporteMinimo());
	}

	@Test
	void calculaEstadisticasSinPedidos() {
		ComercialService service = new ComercialService(
				mock(ComercialDAO.class),
				mock(PedidoDAO.class));

		var estadisticas = service.estadisticasPedidos(List.of());

		assertEquals(0, estadisticas.getNumeroPedidos());
		assertEquals(0.0, estadisticas.getImporteTotal());
		assertEquals(0.0, estadisticas.getImporteMedio());
		assertNull(estadisticas.getImporteMaximo());
		assertNull(estadisticas.getImporteMinimo());
	}
}
