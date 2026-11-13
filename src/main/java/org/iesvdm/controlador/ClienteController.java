package org.iesvdm.controlador;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.iesvdm.modelo.Cliente;
import org.iesvdm.modelo.ClienteComercial;
import org.iesvdm.modelo.Comercial;
import org.iesvdm.servicios.ClienteComercialService;
import org.iesvdm.servicios.ClienteService;
import org.iesvdm.servicios.ComercialService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


@Controller 
public class ClienteController {

    private final ClienteService clienteService;
    private final ComercialService comercialService;
    private final ClienteComercialService clienteComercialService;

    public ClienteController(ClienteService clienteService, ComercialService comercialService,
            ClienteComercialService clienteComercialService) {
        this.clienteService = clienteService;
        this.comercialService = comercialService;
        this.clienteComercialService = clienteComercialService;
    }

    @GetMapping("/clientes")
    public String listar(@RequestParam(required = false) String ciudad, Model model) {
        return mostrarClientes(ciudad, model);
    }

    @GetMapping("/clientes/ciudad/{ciudad}")
    public String listarPorCiudad(@PathVariable String ciudad, Model model) {
        return mostrarClientes(ciudad, model);
    }

    private String mostrarClientes(String ciudad, Model model) {
        String ciudadFiltro = ciudad == null ? null : ciudad.trim();
        List<Cliente> clientes = ciudadFiltro == null || ciudadFiltro.isEmpty()
                ? clienteService.listAll()
                : clienteService.listByCiudad(ciudadFiltro);
        model.addAttribute("clientes", clientes);
        model.addAttribute("ciudad", ciudadFiltro);
        return "clientes";
    }

    @GetMapping("/clientes/{id}")
    public String detalle(Model model, @PathVariable int id) {
        return clienteService.find(id)
                .map(cliente -> {
                    model.addAttribute("cliente", cliente);
                    model.addAttribute("pedidos", clienteService.listPedidosByCliente(id));
                    return "detalle-cliente";
                })
                .orElse("redirect:/clientes");
    }

    @GetMapping("/clientes/crear")
    public String crear(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "crear-cliente";
    }

    @PostMapping("/clientes/crear")
    public RedirectView submitCrear(@ModelAttribute("cliente") Cliente cliente) {
        clienteService.create(cliente);
        return new RedirectView("/clientes");
    }

    @GetMapping("/clientes/editar/{id}")
    public String editar(Model model, @PathVariable int id) {
        return clienteService.find(id)
                .map(cliente -> {
                    prepararEdicion(model, cliente);
                    return "editar-cliente";
                })
                .orElse("redirect:/clientes");
    }

    private void prepararEdicion(Model model, Cliente cliente) {
        List<ClienteComercial> comercialesVinculados =
                clienteComercialService.listByClienteId(cliente.getId());
        Set<Integer> idsVinculados = new HashSet<>();
        for (ClienteComercial vinculo : comercialesVinculados) {
            idsVinculados.add(vinculo.getComercialId());
        }
        List<Comercial> comercialesDisponibles = comercialService.listAll().stream()
                .filter(comercial -> !idsVinculados.contains(comercial.getId()))
                .toList();

        model.addAttribute("cliente", cliente);
        model.addAttribute("comercialesDisponibles", comercialesDisponibles);
        model.addAttribute("comercialesVinculados", comercialesVinculados);
    }

    @PostMapping("/clientes/editar/{id}")
    public RedirectView submitEditar(@PathVariable int id, @ModelAttribute("cliente") Cliente cliente) {
        cliente.setId(id);
        clienteService.update(cliente);
        return new RedirectView("/clientes");
    }

    @PostMapping("/clientes/editar/{id}/comerciales")
    public RedirectView anadirComercial(@PathVariable int id,
            @RequestParam int comercialId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam int prioridad,
            RedirectAttributes redirectAttributes) {
        if (clienteService.find(id).isEmpty() || comercialService.find(comercialId).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El cliente o el comercial no existe.");
        } else if (prioridad < 0) {
            redirectAttributes.addFlashAttribute("error", "La prioridad no puede ser negativa.");
        } else if (clienteComercialService.add(id, comercialId, fecha, prioridad)) {
            redirectAttributes.addFlashAttribute("mensaje", "Comercial añadido al cliente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "El comercial ya está vinculado a este cliente.");
        }
        return new RedirectView("/clientes/editar/" + id);
    }

    @PostMapping("/clientes/editar/{id}/comerciales/{comercialId}/borrar")
    public RedirectView borrarComercial(@PathVariable int id, @PathVariable int comercialId,
            RedirectAttributes redirectAttributes) {
        clienteComercialService.delete(id, comercialId);
        redirectAttributes.addFlashAttribute("mensaje", "Comercial eliminado del cliente.");
        return new RedirectView("/clientes/editar/" + id);
    }

    @PostMapping("/clientes/borrar/{id}")
    public RedirectView submitBorrar(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.delete(id);
            redirectAttributes.addFlashAttribute("mensaje", "El cliente se ha eliminado.");
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar el cliente porque tiene pedidos o comerciales asociados.");
        }
        return new RedirectView("/clientes");
    }
}
