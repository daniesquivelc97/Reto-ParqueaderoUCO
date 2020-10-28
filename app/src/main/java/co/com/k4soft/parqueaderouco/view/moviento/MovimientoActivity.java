package co.com.k4soft.parqueaderouco.view.moviento;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.com.k4soft.parqueaderouco.R;
import co.com.k4soft.parqueaderouco.entidades.Movimiento;
import co.com.k4soft.parqueaderouco.entidades.Tarifa;
import co.com.k4soft.parqueaderouco.persistencia.room.DataBaseHelper;
import co.com.k4soft.parqueaderouco.utilities.ActionBarUtil;
import co.com.k4soft.parqueaderouco.utilities.DateUtil;

public class MovimientoActivity extends AppCompatActivity {

    private DataBaseHelper db;
    private ActionBarUtil actionBarUtil;

    @BindView(R.id.txtPlaca)
    public EditText txtPlaca;
    @BindView(R.id.tipoTarifaSpinner)
    public  Spinner tipoTarifaSpinner;
    @BindView(R.id.btnIngreso)
    public Button btnIngreso;
    @BindView(R.id.btnFechaFinal)
    public Button btnSalida;
    @BindView(R.id.layoutDatos)
    public ConstraintLayout layoutDatos;
    @BindView(R.id.txtCantidadHoras)
    public TextView txtCantidadHoras;
    @BindView(R.id.txtTotal)
    public TextView txtTotal;

    private List<Tarifa> listaTarifas;
    private Movimiento movimiento;
    private Tarifa tarifa;
    private String[] arrayTarifas;
    private Double cantidadTiempo;
    private String cantidadTiempoString;
    private Double totalPagar;
    private static final Integer finalizaMovimiento = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimiento);
        ButterKnife.bind(this);
        initComponents();
        hideComponents();
        cargarSpinner();
        spinnerOnItemSelected();
    }

    private void spinnerOnItemSelected() {
        tipoTarifaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tarifa = listaTarifas.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void cargarSpinner() {
       listaTarifas = db.getTarifaDAO().listar();
        if(listaTarifas.isEmpty()){
            Toast.makeText(getApplication(),R.string.sin_tarifas,Toast.LENGTH_SHORT).show();
            finish();
        }else{
           arrayTarifas = new String[listaTarifas.size()];
            for(int i = 0; i < listaTarifas.size(); i++){
                arrayTarifas[i] = listaTarifas.get(i).getNombre();
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,arrayTarifas);
            tipoTarifaSpinner.setAdapter(arrayAdapter);
        }
    }

    private void hideComponents() {
        tipoTarifaSpinner.setVisibility(View.GONE);
        btnIngreso.setVisibility(View.GONE);
        btnSalida.setVisibility(View.GONE);
        layoutDatos.setVisibility(View.GONE);
    }

    private void initComponents() {
        db = DataBaseHelper.getDBMainThread(this);
        actionBarUtil = new ActionBarUtil(this);
        actionBarUtil.setToolBar(getString(R.string.registrsr_ingreso_salida));
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void buscarPlaca(View view) throws ParseException {
      hideComponents();
      movimiento = db.getMovimientoDAO().findByPLaca(txtPlaca.getText().toString(), finalizaMovimiento);
      if(movimiento == null) {
          showComponentesIngreso();
      }
      else {
          generarInfoMovimiento();
          showComponentesSalida();
      }
    }

    private Double getTarifaById(){
        tarifa = db.getTarifaDAO().findById(movimiento.getIdTarifa());
        return tarifa.getPrecio();
    }

    private void generarInfoMovimiento() throws ParseException {
        calcularTiempo();
        txtCantidadHoras.setText(cantidadTiempoString);
        calcularTotalPagar();
        txtTotal.setText(totalPagar.toString());
    }

    private void calcularTotalPagar() {
        getTarifaById();
        double parteDecimal = cantidadTiempo % 1;
        double parteEntera = cantidadTiempo - parteDecimal;
        totalPagar = 0.0;
        if (parteDecimal > 0){
            totalPagar = totalPagar + 1 * tarifa.getPrecio();
        }
        totalPagar = totalPagar + parteEntera * tarifa.getPrecio();
    }

    private void calcularTiempo() throws ParseException {
        String fechaEntrada = movimiento.getFechaEntrada();
        String fechaSalida = DateUtil.getCurrenDate();
        cantidadTiempo = DateUtil.timeFromDates(fechaEntrada, fechaSalida);
        cantidadTiempoString = DateUtil.timeFromDates(fechaEntrada, fechaSalida).toString();
    }

    private void showComponentesSalida() {
        btnSalida.setVisibility(View.VISIBLE);
        layoutDatos.setVisibility(View.VISIBLE);
    }

    private void showComponentesIngreso() {
        tipoTarifaSpinner.setVisibility(View.VISIBLE);
        btnIngreso.setVisibility(View.VISIBLE);

    }

    public void registrarIngreso(View view) {
        if(tarifa == null){
            Toast.makeText(getApplicationContext(),R.string.debe_seleccionar_tarifa, Toast.LENGTH_SHORT).show();
        }else if(movimiento == null){
            movimiento = new Movimiento();
            movimiento.setPlaca(txtPlaca.getText().toString());
            movimiento.setIdTarifa(tarifa.getIdTarifa());
            movimiento.setFechaEntrada(DateUtil.convertDateToString(new Date()));
            movimiento.setFinalizaMovimiento(true);
            new InsercionMoviento().execute(movimiento);
            restaurarComponentes();
        }
    }

    public void restaurarComponentes(){
        movimiento = null;
        hideComponents();
        txtPlaca.setText("");
    }

    public void registarSalida(View view) {
        movimiento.setIdMovimiento(movimiento.getIdMovimiento());
        movimiento.setFechaSalida(DateUtil.convertDateToString(new Date()));
        movimiento.setPagoFacturado(totalPagar);
        movimiento.setFinalizaMovimiento(false);
        new ActualizacionMovimiento().execute(movimiento);
        restaurarComponentes();
    }

    private class ActualizacionMovimiento extends AsyncTask<Movimiento, Void, Void>{

        @Override
        protected Void doInBackground(Movimiento... movimiento) {
            DataBaseHelper.getSimpleDB(getApplicationContext()).getMovimientoDAO().update(movimiento[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(),R.string.informacion_guardada_exitosamente, Toast.LENGTH_SHORT).show();
        }
    }

    private class InsercionMoviento extends AsyncTask<Movimiento, Void,Void>{

        @Override
        protected Void doInBackground(Movimiento... movimientos) {
            DataBaseHelper.getSimpleDB(getApplicationContext()).getMovimientoDAO().insert(movimientos[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(),R.string.informacion_guardada_exitosamente, Toast.LENGTH_SHORT).show();
        }
    }

}
