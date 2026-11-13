package org.iesvdm.controlador;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.iesvdm.modelo.Cliente;
import org.iesvdm.modelo.ClienteComercial;
import org.iesvdm.modelo.Comercial;
import org.iesvdm.servicios.ClienteComercialService;
import org.iesvdm.servicios.ClienteService;
import org.iesvdm.servicios.ComercialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ClienteControllerTest {

	private ClienteService clienteService;
	private ComercialService comercialService;
	private ClienteComercialService clienteComercialService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		clienteService = mock(ClienteService.class);
		comercialService = mock(ComercialService.class);
		clienteComercialService = mock(ClienteComercialService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(
				new ClienteController(clienteService, comercialService, clienteComercialService))
				.setViewResolvers((viewName, locale) -> (model, request, response) -> {
				})
				.build();
	}

	@Test
	void filtraClientesPorParametroDeConsulta() throws Exception {
		List<Cliente> clientes = List.of(new Cliente(1, "Ana", "López", null, "Sevilla", 1));
		when(clienteService.listByCiudad("Sevilla")).thenReturn(clientes);

		mockMvc.perform(get("/clientes").param("ciudad", "Sevilla"))
				.andExpect(status().isOk())
				.andExpect(view().name("clientes"))
				.andExpect(model().attribute("clientes", clientes))
				.andExpect(model().attribute("ciudad", "Sevilla"));

		verify(clienteService).listByCiudad("Sevilla");
	}

	@Test
	void filtraClientesPorVariableDeRuta() throws Exception {
		List<Cliente> clientes = List.of(new Cliente(2, "Luis", "Pérez", null, "Sevilla", 2));
		when(clienteService.listByCiudad("Sevilla")).thenReturn(clientes);

		mockMvc.perform(get("/clientes/ciudad/Sevilla"))
				.andExpect(status().isOk())
				.andExpect(view().name("clientes"))
				.andExpect(model().attribute("clientes", clientes))
				.andExpect(model().attribute("ciudad", "Sevilla"));

		verify(clienteService).listByCiudad("Sevilla");
	}

	@Test
	void editarMuestraComercialesVinculadosYDisponibles() throws Exception {
		Cliente cliente = new Cliente(1, "Ana", "López", null, "Sevilla", 1);
		ClienteComercial vinculado = new ClienteComercial(1, 2, "Luis", "Pérez", null,
				LocalDate.of(2025, 1, 10), 1);
		List<Comercial> comerciales = List.of(
				new Comercial(2, "Luis", "Pérez", null, 0.1f),
				new Comercial(3, "Eva", "Díaz", null, 0.12f));
		when(clienteService.find(1)).thenReturn(Optional.of(cliente));
		when(clienteComercialService.listByClienteId(1)).thenReturn(List.of(vinculado));
		when(comercialService.listAll()).thenReturn(comerciales);

		mockMvc.perform(get("/clientes/editar/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("editar-cliente"))
				.andExpect(model().attribute("comercialesVinculados", List.of(vinculado)))
				.andExpect(model().attribute("comercialesDisponibles", List.of(comerciales.get(1))));
	}

	@Test
	void anadirComercialGuardaElVinculoYRedirigeAlCliente() throws Exception {
		when(clienteService.find(1)).thenReturn(Optional.of(
				new Cliente(1, "Ana", "López", null, "Sevilla", 1)));
		when(comercialService.find(2)).thenReturn(Optional.of(
				new Comercial(2, "Luis", "Pérez", null, 0.1f)));
		when(clienteComercialService.add(1, 2, LocalDate.of(2025, 1, 10), 1)).thenReturn(true);

		mockMvc.perform(post("/clientes/editar/1/comerciales")
						.param("comercialId", "2")
						.param("fecha", "2025-01-10")
						.param("prioridad", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
						.redirectedUrl("/clientes/editar/1"));

		verify(clienteComercialService).add(1, 2, LocalDate.of(2025, 1, 10), 1);
	}

	@Test
	void borrarComercialEliminaElVinculoYRedirigeAlCliente() throws Exception {
		mockMvc.perform(post("/clientes/editar/1/comerciales/2/borrar"))
				.andExpect(status().is3xxRedirection())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
						.redirectedUrl("/clientes/editar/1"));

		verify(clienteComercialService).delete(1, 2);
	}
}
