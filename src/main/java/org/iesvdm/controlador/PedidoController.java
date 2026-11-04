package org.iesvdm.controlador;

import org.iesvdm.modelo.Pedido;
import org.iesvdm.servicios.ClienteService;
import org.iesvdm.servicios.ComercialService;
import org.iesvdm.servicios.PedidoService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PedidoController {

	private final PedidoService pedidoService;
	private final ClienteService clienteService;
	private final ComercialService comercialService;

	public PedidoController(PedidoService pedidoService, ClienteService clienteService,
			ComercialService comercialService) {
		this.pedidoService = pedidoService;
		this.clienteService = clienteService;
		this.comercialService = comercialService;
	}

	@GetMapping("/pedidos")
	public String listar(Model model) {
		model.addAttribute("pedidos", pedidoService.listAllDetails());
		return "pedidos";
	}

	@GetMapping("/pedidos/{id}")
	public String detalle(Model model, @PathVariable int id) {
		return pedidoService.findDetail(id)
				.map(pedido -> {
					model.addAttribute("pedido", pedido);
					return "detalle-pedido";
				})
				.orElse("redirect:/pedidos");
	}

	@GetMapping("/pedidos/crear")
	public String crear(Model model) {
		model.addAttribute("pedido", new Pedido());
		cargarOpciones(model);
		return "crear-pedido";
	}

	@PostMapping("/pedidos/crear")
	public String submitCrear(@ModelAttribute("pedido") Pedido pedido, Model model) {
		if (!relacionesValidas(pedido)) {
			model.addAttribute("error", "Selecciona un cliente y un comercial válidos.");
			cargarOpciones(model);
			return "crear-pedido";
		}

		try {
			pedidoService.create(pedido);
			return "redirect:/pedidos";
		} catch (DataIntegrityViolationException exception) {
			model.addAttribute("error", "No se pudo guardar el pedido porque el cliente o el comercial ya no existe.");
			cargarOpciones(model);
			return "crear-pedido";
		}
	}

	@GetMapping("/pedidos/editar/{id}")
	public String editar(Model model, @PathVariable int id) {
		return pedidoService.find(id)
				.map(pedido -> {
					model.addAttribute("pedido", pedido);
					cargarOpciones(model);
					return "editar-pedido";
				})
				.orElse("redirect:/pedidos");
	}

	@PostMapping("/pedidos/editar/{id}")
	public String submitEditar(@PathVariable int id, @ModelAttribute("pedido") Pedido pedido, Model model) {
		if (pedidoService.find(id).isEmpty()) {
			return "redirect:/pedidos";
		}
		pedido.setId(id);
		if (!relacionesValidas(pedido)) {
			model.addAttribute("error", "Selecciona un cliente y un comercial válidos.");
			cargarOpciones(model);
			return "editar-pedido";
		}

		try {
			pedidoService.update(pedido);
			return "redirect:/pedidos";
		} catch (DataIntegrityViolationException exception) {
			model.addAttribute("error", "No se pudo guardar el pedido porque el cliente o el comercial ya no existe.");
			cargarOpciones(model);
			return "editar-pedido";
		}
	}

	@PostMapping("/pedidos/borrar/{id}")
	public RedirectView submitBorrar(@PathVariable int id, RedirectAttributes redirectAttributes) {
		pedidoService.delete(id);
		redirectAttributes.addFlashAttribute("mensaje", "El pedido se ha eliminado.");
		return new RedirectView("/pedidos");
	}

	private boolean relacionesValidas(Pedido pedido) {
		return pedido.getClienteId() != null
				&& pedido.getComercialId() != null
				&& clienteService.find(pedido.getClienteId()).isPresent()
				&& comercialService.find(pedido.getComercialId()).isPresent();
	}

	private void cargarOpciones(Model model) {
		model.addAttribute("clientes", clienteService.listAll());
		model.addAttribute("comerciales", comercialService.listAll());
	}
}
