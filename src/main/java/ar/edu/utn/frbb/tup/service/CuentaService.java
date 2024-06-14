package ar.edu.utn.frbb.tup.service;

import ar.edu.utn.frbb.tup.model.Cliente;
import ar.edu.utn.frbb.tup.model.Cuenta;
import ar.edu.utn.frbb.tup.model.TipoCuenta;
import ar.edu.utn.frbb.tup.model.TipoMoneda;
import ar.edu.utn.frbb.tup.model.exception.ClienteNoExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.model.exception.CuentaNoSoportadaException;
import ar.edu.utn.frbb.tup.model.exception.TipoCuentaAlreadyExistsException;
import ar.edu.utn.frbb.tup.persistence.ClienteDao;
import ar.edu.utn.frbb.tup.persistence.CuentaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CuentaService {
    CuentaDao cuentaDao = new CuentaDao();

    CuentaService cuentaService;
    @Autowired
    public CuentaService(CuentaDao cuentaDao) {
        this.cuentaDao = cuentaDao;
    }

    public void darDeAltaCuenta(Cuenta cuenta, long dni) throws CuentaNoSoportadaException, CuentaAlreadyExistsException, ClienteNoExistsException {
        // Verificar si la cuenta es soportada
        if (cuenta.getTipoCuenta() == TipoCuenta.CUENTA_CORRIENTE && cuenta.getMoneda() == TipoMoneda.DOLARES) {
            throw new CuentaNoSoportadaException("Cuenta corriente en dólares no soportada");
        }

        // Verificar si la cuenta ya existe
        Cuenta cuentaExistente = cuentaDao.find(cuenta.getNumeroCuenta());
        if (cuentaExistente != null) {
            throw new CuentaAlreadyExistsException("La cuenta " + cuenta.getNumeroCuenta() + " ya existe.");
        }

        // Verificar si el cliente existe (asumiendo que el ClienteService tiene un método para verificar esto)
        Cliente cliente = cuenta.getTitular();
        if (cliente == null || cliente.getDni() != dni) {
            throw new ClienteNoExistsException("Cliente con DNI " + dni + " no existe.");
        }

        // Guardar la nueva cuenta
        cuentaDao.save(cuenta);
    }
    

    public Cuenta find(long id) {
        return cuentaDao.find(id);
    }

    public boolean tipoCuentaEstaSoportada(Cuenta cuenta) {
        if (cuenta.getTipoCuenta().equals(TipoCuenta.CUENTA_CORRIENTE) && cuenta.getMoneda().equals(TipoMoneda.DOLARES))  {
            return false;
        }
        return true;
    }
}
