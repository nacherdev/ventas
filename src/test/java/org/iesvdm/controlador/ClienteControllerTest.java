package org.iesvdm.controlador;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.iesvdm.modelo.Cliente;
import org.iesvdm.servicios.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ClienteControllerTest {

	private ClienteService clienteService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		clienteService = mock(ClienteService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new ClienteController(clienteService))
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
}
