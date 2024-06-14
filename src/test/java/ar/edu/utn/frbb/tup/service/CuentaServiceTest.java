package ar.edu.utn.frbb.tup.service;

import org.springframework.beans.factory.annotation.Autowired;
import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CuentaServiceTest {

    @Mock
    private ClienteDao clienteDao;

    @Mock
    private CuentaDao cuentaDao;

    @Autowired
    private ClienteService clienteService;

    @InjectMocks
    private CuentaService cuentaService;

    @BeforeEach
    public void setUp() {
        cuentaService = new CuentaService(cuentaDao);
        clienteService = new ClienteService(clienteDao);
    }

    private Cliente crearCliente() {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456439);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3, 25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);
        return pepeRino;
    }

    private Cuenta crearCuenta(Cliente pepeRino, TipoCuenta tipoCuenta, TipoMoneda tipoMoneda, int balance) {
        Cuenta cuenta = new Cuenta();
        cuenta.setTitular(pepeRino);
        cuenta.setNumeroCuenta(12345678);
        cuenta.setMoneda(tipoMoneda);
        cuenta.setBalance(balance);
        cuenta.setTipoCuenta(tipoCuenta);
        return cuenta;
    }

    @Test
    public void testCuentaAlreadyExistsException() throws TipoCuentaAlreadyExistsException, ClienteNoExistsException, CuentaNoSoportadaException, CuentaAlreadyExistsException, ClienteAlreadyExistsException, ClienteNotFoundException {
        Cliente pepeRino = crearCliente();
        Cuenta cuenta = crearCuenta(pepeRino, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 500000);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(cuenta);

        assertThrows(CuentaAlreadyExistsException.class, () -> cuentaService.darDeAltaCuenta(cuenta, 26456439));
    }

    @Test
    public void testCuentaNoSoportadaException() throws TipoCuentaAlreadyExistsException, ClienteNoExistsException, CuentaNoSoportadaException, CuentaAlreadyExistsException, ClienteNotFoundException {
        Cliente pepeRino = crearCliente();
        Cuenta cuenta = crearCuenta(pepeRino, TipoCuenta.CUENTA_CORRIENTE, TipoMoneda.DOLARES, 0);

        assertThrows(CuentaNoSoportadaException.class, () -> cuentaService.darDeAltaCuenta(cuenta, pepeRino.getDni()));
    }


    @Test
    public void testClienteYaTieneCuentaDeEsteTipo() throws TipoCuentaAlreadyExistsException, ClienteNoExistsException, CuentaNoSoportadaException, CuentaAlreadyExistsException, ClienteAlreadyExistsException, ClienteNotFoundException {
        Cliente pepeRino = crearCliente();
        Cuenta cuenta = crearCuenta(pepeRino, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 500000);

        when(clienteDao.find(pepeRino.getDni(), true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuenta, pepeRino.getDni());

        Cuenta cuenta2 = crearCuenta(pepeRino, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 50000);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> clienteService.agregarCuenta(cuenta2, pepeRino.getDni()));

        verify(clienteDao, times(1)).save(pepeRino);

        assertEquals(1, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuenta.getTitular());
    }

    @Test
    public void testCuentaCreadaSuccess() throws TipoCuentaAlreadyExistsException, ClienteNoExistsException, CuentaNoSoportadaException, CuentaAlreadyExistsException, ClienteAlreadyExistsException, ClienteNotFoundException {
        Cliente pepeRino = crearCliente();
        Cuenta cuenta = crearCuenta(pepeRino, TipoCuenta.CAJA_AHORRO, TipoMoneda.PESOS, 500000);

        when(cuentaDao.find(cuenta.getNumeroCuenta())).thenReturn(null);
        when(clienteDao.find(pepeRino.getDni(), true)).thenReturn(pepeRino);

        cuentaService.darDeAltaCuenta(cuenta, pepeRino.getDni());

        clienteService.agregarCuenta(cuenta, pepeRino.getDni());

        verify(cuentaDao, times(1)).save(cuenta);
        verify(clienteDao, times(1)).save(pepeRino);

        assertEquals(1, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuenta.getTitular());
    }
}
