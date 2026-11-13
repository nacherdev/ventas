package org.iesvdm.dao;

import java.time.LocalDate;
import java.util.List;

import org.iesvdm.modelo.ClienteComercial;

public interface ClienteComercialDAO {
	List<ClienteComercial> findByClienteId(int clienteId);
	boolean exists(int clienteId, int comercialId);
	void add(int clienteId, int comercialId, LocalDate fecha, int prioridad);
	void delete(int clienteId, int comercialId);
}
