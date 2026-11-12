package org.iesvdm.dao;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

import org.iesvdm.modelo.Pedido;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PedidoDAOImpl implements PedidoDAO {

	private static final String DETALLE_QUERY = """
			SELECT p.id, p.total, p.fecha, p.id_cliente AS cliente_id,
			       CONCAT_WS(' ', c.nombre, c.apellido1, c.apellido2) AS cliente_nombre,
			       CONCAT_WS(' ', co.nombre, co.apellido1, co.apellido2) AS comercial_nombre
			FROM pedido p
			JOIN cliente c ON p.id_cliente = c.id
			JOIN comercial co ON p.id_comercial = co.id
			""";
	private static final RowMapper<PedidoDetalle> PEDIDO_DETALLE_ROW_MAPPER = (rs, rowNum) -> new PedidoDetalle(
			rs.getInt("id"),
			rs.getObject("total", Double.class),
			rs.getDate("fecha") == null ? null : rs.getDate("fecha").toLocalDate(),
			rs.getString("cliente_nombre"),
			rs.getString("comercial_nombre"),
			rs.getInt("cliente_id"));

	private final JdbcTemplate jdbcTemplate;

	public PedidoDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void create(Pedido pedido) {
		String sql = """
				INSERT INTO pedido (total, fecha, id_cliente, id_comercial)
				VALUES (?, ?, ?, ?)
				""";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rows = jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(sql, new String[] { "id" });
			statement.setObject(1, pedido.getTotal(), Types.DOUBLE);
			statement.setObject(2, pedido.getFecha(), Types.DATE);
			statement.setObject(3, pedido.getClienteId(), Types.INTEGER);
			statement.setObject(4, pedido.getComercialId(), Types.INTEGER);
			return statement;
		}, keyHolder);

		if (keyHolder.getKey() != null) {
			pedido.setId(keyHolder.getKey().intValue());
		}
		log.info("Insertados {} registros de Pedido.", rows);
	}

	@Override
	public List<PedidoDetalle> getAllDetails() {
		List<PedidoDetalle> pedidos = jdbcTemplate.query(DETALLE_QUERY + " ORDER BY p.id",
				PEDIDO_DETALLE_ROW_MAPPER);
		log.info("Devueltos {} registros de Pedido.", pedidos.size());
		return pedidos;
	}

	@Override
	public List<PedidoDetalle> findByComercialId(int comercialId) {
		return jdbcTemplate.query(
				DETALLE_QUERY + " WHERE p.id_comercial = ? ORDER BY p.id",
				PEDIDO_DETALLE_ROW_MAPPER,
				comercialId);
	}

	@Override
	public List<PedidoDetalle> findByClienteId(int clienteId) {
		return jdbcTemplate.query(
				DETALLE_QUERY + " WHERE p.id_cliente = ? ORDER BY p.id",
				PEDIDO_DETALLE_ROW_MAPPER,
				clienteId);
	}

	@Override
	public Optional<Pedido> find(int id) {
		List<Pedido> pedidos = jdbcTemplate.query("""
				SELECT id, total, fecha, id_cliente, id_comercial
				FROM pedido WHERE id = ?
				""",
				(rs, rowNum) -> new Pedido(
						rs.getInt("id"),
						rs.getObject("total", Double.class),
						rs.getDate("fecha") == null ? null : rs.getDate("fecha").toLocalDate(),
						rs.getInt("id_cliente"),
						rs.getInt("id_comercial")),
				id);
		return pedidos.stream().findFirst();
	}

	@Override
	public Optional<PedidoDetalle> findDetail(int id) {
		List<PedidoDetalle> pedidos = jdbcTemplate.query(
				DETALLE_QUERY + " WHERE p.id = ?",
				PEDIDO_DETALLE_ROW_MAPPER,
				id);
		return pedidos.stream().findFirst();
	}

	@Override
	public void update(Pedido pedido) {
		int rows = jdbcTemplate.update("""
				UPDATE pedido
				SET total = ?, fecha = ?, id_cliente = ?, id_comercial = ?
				WHERE id = ?
				""",
				pedido.getTotal(),
				pedido.getFecha(),
				pedido.getClienteId(),
				pedido.getComercialId(),
				pedido.getId());
		log.info("Update de Pedido con {} registros actualizados.", rows);
	}

	@Override
	public void delete(int id) {
		int rows = jdbcTemplate.update("DELETE FROM pedido WHERE id = ?", id);
		log.info("Delete de Pedido con {} registros eliminados.", rows);
	}
}
