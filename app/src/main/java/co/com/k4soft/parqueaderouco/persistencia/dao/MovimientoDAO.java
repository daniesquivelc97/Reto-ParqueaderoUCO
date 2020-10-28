package co.com.k4soft.parqueaderouco.persistencia.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import co.com.k4soft.parqueaderouco.entidades.Movimiento;

@Dao
public interface MovimientoDAO {

    @Query("SELECT * FROM MOVIMIENTO Where placa=:placa AND finalizaMovimiento=:finalizaMovimiento")
    Movimiento  findByPLaca(String placa, Integer finalizaMovimiento);

    @Insert
    void insert(Movimiento movimiento);

    @Update
    void update(Movimiento movimiento);

    //@Query("SELECT * FROM MOVIMIENTO WHERE (fechaEntrada >= :fechaInicio AND fechaEntrada <= :fechaFinal AND (fechaSalida IS NULL OR fechaSalida <= :fechaFinal)) ORDER BY fechaEntrada ASC")
    @Query("SELECT * FROM MOVIMIENTO WHERE (fechaEntrada BETWEEN :fechaInicio AND :fechaFinal) ORDER BY fechaEntrada ASC")
    List<Movimiento> findByFecha(String fechaInicio, String fechaFinal);
}
