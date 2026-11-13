package org.iesvdm.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasPedidosDTO {
	private long numeroPedidos;
	private double importeTotal;
	private double importeMedio;
	private Double importeMaximo;
	private Double importeMinimo;
}
