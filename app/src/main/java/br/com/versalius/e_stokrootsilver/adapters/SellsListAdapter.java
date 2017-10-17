package br.com.versalius.e_stokrootsilver.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.StringTokenizer;

import br.com.versalius.e_stokrootsilver.R;
import br.com.versalius.e_stokrootsilver.model.Sell;

/**
 * Created by Giovanne on 17/10/2017.
 */

public class SellsListAdapter extends RecyclerView.Adapter<SellsListAdapter.ViewHolder> {

    private Context context;
    private List<Sell> list;
    private LayoutInflater inflater;

    public SellsListAdapter(Context context, List<Sell> list) {
        this.context = context;
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_sell, parent, false));

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvClient.setText(list.get(position).getClient());
        holder.tvDate.setText(list.get(position).getDate());
        holder.tvTotalPrice.setText("R$ " + list.get(position).getTotalPrice());
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvClient, tvTotalPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvClient = itemView.findViewById(R.id.tvClient);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Click:"+getAdapterPosition(),Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
