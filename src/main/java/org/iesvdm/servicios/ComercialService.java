package org.iesvdm.servicios;

import java.util.List;
import java.util.Optional;

import org.iesvdm.dao.ComercialDAO;
import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.modelo.Comercial;
import org.iesvdm.modelo.ClienteTotalPedidosDTO;
import org.iesvdm.modelo.EstadisticasPedidosDTO;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.stereotype.Service;

@Service
public class ComercialService {

	private final ComercialDAO comercialDAO;
	private final PedidoDAO pedidoDAO;

	public ComercialService(ComercialDAO comercialDAO, PedidoDAO pedidoDAO) {
		this.comercialDAO = comercialDAO;
		this.pedidoDAO = pedidoDAO;
	}

	public void create(Comercial comercial) {
		comercialDAO.create(comercial);
	}

	public List<Comercial> listAll() {
		return comercialDAO.getAll();
	}

	public Optional<Comercial> find(int id) {
		return comercialDAO.find(id);
	}

	public List<PedidoDetalle> listPedidosByComercial(int comercialId) {
		return pedidoDAO.findByComercialId(comercialId);
	}

	public EstadisticasPedidosDTO estadisticasPedidos(List<PedidoDetalle> pedidos) {
		long numeroPedidos = pedidos.size();
		double importeTotal = 0;
		Double importeMaximo = null;
		Double importeMinimo = null;
		for (PedidoDetalle pedido : pedidos) {
			Double importePedido = pedido.getTotal();
			if (importePedido == null) {
				continue;
			}
			importeTotal += importePedido;
			if (importeMaximo == null || importePedido > importeMaximo) {
				importeMaximo = importePedido;
			}
			if (importeMinimo == null || importePedido < importeMinimo) {
				importeMinimo = importePedido;
			}
		}

		return new EstadisticasPedidosDTO(
				numeroPedidos,
				importeTotal,
				numeroPedidos == 0 ? 0 : importeTotal / numeroPedidos,
				importeMaximo,
				importeMinimo);
	}

	public List<ClienteTotalPedidosDTO> listClientesByComercialOrderByImporteTotal(int comercialId) {
		return pedidoDAO.findClientesByComercialIdOrderByImporteTotal(comercialId);
	}

	public void update(Comercial comercial) {
		comercialDAO.update(comercial);
	}

	public void delete(int id) {
		comercialDAO.delete(id);
	}
}