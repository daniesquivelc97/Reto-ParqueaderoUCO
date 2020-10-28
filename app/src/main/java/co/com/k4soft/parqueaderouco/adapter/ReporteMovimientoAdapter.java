package co.com.k4soft.parqueaderouco.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.com.k4soft.parqueaderouco.entidades.Movimiento;
import co.com.k4soft.parqueaderouco.R;

public class ReporteMovimientoAdapter extends BaseAdapter implements Filterable{

    private final Context context;
    private List<Movimiento> listaMovimientoIn;
    private List<Movimiento> listaMovimientoOut;
    private LayoutInflater inflater;
    double finalPrice = 0;
    double total = 0;

    public ReporteMovimientoAdapter(Context context, List<Movimiento> movimientoIn){
        this.context =context;
        this.listaMovimientoIn = movimientoIn;
        listaMovimientoOut = movimientoIn;
        inflater = LayoutInflater.from(context);
        calculateTotal();
        showTotalToast();
    }

    @Override
    public int getCount() {
        return listaMovimientoOut.size();
    }

    @Override
    public Movimiento getItem(int position) {
        return listaMovimientoOut.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.movimiento_item_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        }
        if(listaMovimientoOut.get(position).getFechaSalida() != null){
            holder.cardViewReporteMovimiento.setVisibility(View.VISIBLE);
            holder.txtPlaca.setText(listaMovimientoOut.get(position).getPlaca());
            holder.txtFechaIngreso.setText(listaMovimientoOut.get(position).getFechaEntrada());
            holder.txtFechaSalida.setText(listaMovimientoOut.get(position).getFechaSalida());
            holder.txtPrecio.setText(Double.toString(listaMovimientoOut.get(position).getPagoFacturado()));
        }
        else {
            holder.cardViewReporteMovimiento.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public void showTotalToast() {
        this.total = this.finalPrice;
        Toast.makeText(context, "Total facturado: " + "$" + this.total, Toast.LENGTH_LONG).show();
    }

    public double calculateTotal() {
        for(int i=0; i<listaMovimientoOut.size(); i++){
            this.finalPrice += listaMovimientoOut.get(i).getPagoFacturado();
        }
        return this.finalPrice;
    }


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listaMovimientoOut = (List<Movimiento>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<Movimiento> FilteredArrList = new ArrayList<>();
                if (listaMovimientoIn == null) {
                    listaMovimientoIn = new ArrayList<>(listaMovimientoOut);
                }
                if (constraint == null || constraint.length() == 0) {
                    results.count = listaMovimientoIn.size();
                    results.values = listaMovimientoIn;
                } else {

                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < listaMovimientoIn.size(); i++) {
                        String data = listaMovimientoIn.get(i).getPlaca();
                        if (data.toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(listaMovimientoIn.get(i));
                        }
                    }
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
        return filter;
    }

    class ViewHolder{
        @BindView(R.id.txtPlaca)
        TextView txtPlaca;
        @BindView(R.id.txtFechaIngreso)
        TextView txtFechaIngreso;
        @BindView(R.id.txtFechaSalida)
        TextView txtFechaSalida;
        @BindView(R.id.txtPrecio)
        TextView txtPrecio;
        @BindView(R.id.cardViewReporteMovimiento)
        CardView cardViewReporteMovimiento;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
