package org.iesvdm.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteTotalPedidosDTO {
	private int clienteId;
	private String clienteNombre;
	private double importeTotal;
}
