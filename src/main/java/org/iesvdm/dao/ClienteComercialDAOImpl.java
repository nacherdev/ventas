package org.iesvdm.dao;

import java.time.LocalDate;
import java.util.List;

import org.iesvdm.modelo.ClienteComercial;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ClienteComercialDAOImpl implements ClienteComercialDAO {

	private final JdbcTemplate jdbcTemplate;

	ClienteComercialDAOImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<ClienteComercial> findByClienteId(int clienteId) {
		return jdbcTemplate.query("""
				SELECT vinculo.cliente_id, vinculo.comercial_id,
				       comercial.nombre, comercial.apellido1, comercial.apellido2,
				       vinculo.fecha, vinculo.prioridad
				FROM cliente_has_comercial AS vinculo
				JOIN comercial ON comercial.id = vinculo.comercial_id
				WHERE vinculo.cliente_id = ?
				ORDER BY vinculo.prioridad, comercial.apellido1, comercial.nombre
				""",
				(rs, rowNum) -> {
					java.sql.Date fecha = rs.getDate("fecha");
					return new ClienteComercial(
							rs.getInt("cliente_id"),
							rs.getInt("comercial_id"),
							rs.getString("nombre"),
							rs.getString("apellido1"),
							rs.getString("apellido2"),
							fecha == null ? null : fecha.toLocalDate(),
							rs.getInt("prioridad"));
				},
				clienteId);
	}

	@Override
	public boolean exists(int clienteId, int comercialId) {
		Integer count = jdbcTemplate.queryForObject("""
				SELECT COUNT(*) FROM cliente_has_comercial
				WHERE cliente_id = ? AND comercial_id = ?
				""", Integer.class, clienteId, comercialId);
		return count != null && count > 0;
	}

	@Override
	public void add(int clienteId, int comercialId, LocalDate fecha, int prioridad) {
		int rows = jdbcTemplate.update("""
				INSERT INTO cliente_has_comercial (cliente_id, comercial_id, fecha, prioridad)
				VALUES (?, ?, ?, ?)
				""", clienteId, comercialId, fecha, prioridad);
		log.info("Añadido vínculo cliente-comercial ({} registros).", rows);
	}

	@Override
	public void delete(int clienteId, int comercialId) {
		int rows = jdbcTemplate.update("""
				DELETE FROM cliente_has_comercial
				WHERE cliente_id = ? AND comercial_id = ?
				""", clienteId, comercialId);
		log.info("Eliminado vínculo cliente-comercial ({} registros).", rows);
	}
}
