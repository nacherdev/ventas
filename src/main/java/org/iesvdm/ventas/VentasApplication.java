package org.iesvdm.ventas;

import org.iesvdm.dao.ClienteDAO;
import org.iesvdm.modelo.Cliente;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "org.iesvdm")
@Slf4j
public class VentasApplication implements CommandLineRunner {

    private final ClienteDAO clienteDAO;

    // Inyectamos el DAO directamente
    public VentasApplication(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public static void main(String[] args) {
        SpringApplication.run(VentasApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Probamos el método getAll() del DAO al arrancar la app
        int totalClientes = clienteDAO.getAll().size();
        log.info("¡Clientes disponibles en la base de datos: {}!", totalClientes);

        // O si quieres ver los datos por consola:
        for (Cliente cliente : clienteDAO.getAll()) {
            log.info("Cliente: {} {}", cliente.getNombre(), cliente.getApellido1());
        }
    }
}