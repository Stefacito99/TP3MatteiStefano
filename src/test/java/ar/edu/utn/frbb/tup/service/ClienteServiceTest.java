package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.*;
import ar.edu.utn.frbb.tup.model.exception.ClienteAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.ClienteNotFoundException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.ClienteNotFoundException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClienteServiceTest {

    @Mock
    private ClienteDao clienteDao;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeAll
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testClienteMenor18Años() {
        Cliente clienteMenorDeEdad = new Cliente();
        clienteMenorDeEdad.setFechaNacimiento(LocalDate.of(2020, 2, 7));
        assertThrows(IllegalArgumentException.class, () -> clienteService.darDeAltaCliente(clienteMenorDeEdad));
    }

    @Test
    public void testClienteSuccess() throws ClienteAlreadyExistsException {
        Cliente cliente = new Cliente();
        cliente.setFechaNacimiento(LocalDate.of(1978,3,25));
        cliente.setDni(29857643);
        cliente.setTipoPersona(TipoPersona.PERSONA_FISICA);
        clienteService.darDeAltaCliente(cliente);

        verify(clienteDao, times(1)).save(cliente);
    }

    @Test
    public void testClienteAlreadyExistsException() throws ClienteAlreadyExistsException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456437);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(clienteDao.find(26456437, false)).thenReturn(new Cliente());

        assertThrows(ClienteAlreadyExistsException.class, () -> clienteService.darDeAltaCliente(pepeRino));
    }



    @Test
    public void testAgregarCuentaAClienteSuccess() throws TipoCuentaAlreadyExistsException, ClienteNotFoundException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456439);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuenta, pepeRino.getDni());

        verify(clienteDao, times(1)).save(pepeRino);

        assertEquals(1, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuenta.getTitular());

    }


    @Test
    public void testAgregarCuentaAClienteDuplicada() throws TipoCuentaAlreadyExistsException, ClienteNotFoundException {
        Cliente luciano = new Cliente();
        luciano.setDni(26456439);
        luciano.setNombre("Pepe");
        luciano.setApellido("Rino");
        luciano.setFechaNacimiento(LocalDate.of(1978, 3,25));
        luciano.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuenta = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456439, true)).thenReturn(luciano);

        clienteService.agregarCuenta(cuenta, luciano.getDni());

        Cuenta cuenta2 = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        assertThrows(TipoCuentaAlreadyExistsException.class, () -> clienteService.agregarCuenta(cuenta2, luciano.getDni()));
        verify(clienteDao, times(1)).save(luciano);
        assertEquals(1, luciano.getCuentas().size());
        assertEquals(luciano, cuenta.getTitular());

    }

    //Agregar una CA$ y CC$ --> success 2 cuentas, titular peperino
    @Test
    public void testAgregarCAyCC() throws TipoCuentaAlreadyExistsException, ClienteNotFoundException{
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456440);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuentaCA = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        Cuenta cuentaCC = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(1000000)
                .setTipoCuenta(TipoCuenta.CUENTA_CORRIENTE);

        when(clienteDao.find(26456440, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuentaCA, pepeRino.getDni());
        clienteService.agregarCuenta(cuentaCC, pepeRino.getDni());

        verify(clienteDao, times(2)).save(pepeRino);

        assertEquals(2, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuentaCA.getTitular());
        assertEquals(pepeRino, cuentaCC.getTitular());
    }

    //Agregar una CA$ y CAU$S --> success 2 cuentas, titular peperino...
    @Test
    public void testAgregarCAyCAUSD() throws TipoCuentaAlreadyExistsException, ClienteNotFoundException{
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456441);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        Cuenta cuentaCA = new Cuenta()
                .setMoneda(TipoMoneda.PESOS)
                .setBalance(500000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        Cuenta cuentaCAUSD = new Cuenta()
                .setMoneda(TipoMoneda.DOLARES)
                .setBalance(20000)
                .setTipoCuenta(TipoCuenta.CAJA_AHORRO);

        when(clienteDao.find(26456441, true)).thenReturn(pepeRino);

        clienteService.agregarCuenta(cuentaCA, pepeRino.getDni());
        clienteService.agregarCuenta(cuentaCAUSD, pepeRino.getDni());

        verify(clienteDao, times(2)).save(pepeRino);

        assertEquals(2, pepeRino.getCuentas().size());
        assertEquals(pepeRino, cuentaCA.getTitular());
        assertEquals(pepeRino, cuentaCAUSD.getTitular());
    }

    //Testear clienteService.buscarPorDni
    //Caso de Exito
    @Test
    public void testBuscarPorDniSuccess() throws ClienteNotFoundException {
        Cliente pepeRino = new Cliente();
        pepeRino.setDni(26456442);
        pepeRino.setNombre("Pepe");
        pepeRino.setApellido("Rino");
        pepeRino.setFechaNacimiento(LocalDate.of(1978, 3,25));
        pepeRino.setTipoPersona(TipoPersona.PERSONA_FISICA);

        when(clienteDao.find(26456442, true)).thenReturn(pepeRino);

        Cliente clienteEncontrado = clienteService.buscarClientePorDni(26456442);

        assertNotNull(clienteEncontrado);
        assertEquals(pepeRino, clienteEncontrado);
    }

    //Caso de Falla
    @Test
    public void testBuscarPorDniFailure() throws ClienteNotFoundException {
        when(clienteDao.find(eq(10000000L), eq(true))).thenReturn(null);

        assertThrows(ClienteNotFoundException.class, () -> clienteService.buscarClientePorDni(10000000L));
    }
}