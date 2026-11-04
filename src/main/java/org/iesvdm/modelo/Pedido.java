package org.iesvdm.modelo;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pedido {
	private int id;
	private Double total;
	private LocalDate fecha;
	private Integer clienteId;
	private Integer comercialId;
}
