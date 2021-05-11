package com.hazard.projets4;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hazard.projets4.Model.Cart;
import com.hazard.projets4.Prevalent.Prevalent;
import com.hazard.projets4.ViewHolder.CartViewHolder;

public class Cartactivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private Button NextProcessBtn;
    private TextView textTotalAmount ,tstMsg1;

    private int overTotalPrice=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartactivity);


        recyclerView=findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        NextProcessBtn=(Button)findViewById(R.id.next_process_btn);
        textTotalAmount =(TextView)findViewById(R.id.total_price);
        tstMsg1 =(TextView)findViewById(R.id.msg1);

        NextProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                Intent intent=new Intent(Cartactivity.this,ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price",String.valueOf(overTotalPrice));
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        checkOrderState();


        final DatabaseReference cartListRef= FirebaseDatabase.getInstance().getReference().child("cart List");
        FirebaseRecyclerOptions<Cart> options=
                new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(cartListRef.child("Users View")
                .child(Prevalent.currentOnlineUser.getPhone()).
                                child("Products"),Cart.class)
                                .build();

        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter
            =new FirebaseRecyclerAdapter<Cart,CartViewHolder>(options) {

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }


            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder holder, int i, @NonNull Cart cart) {
                holder.txtProductName.setText(cart.getPname());
                holder.txtProductPrice.setText("Price :"+cart.getPrice()+"$");
                holder.txtProductQuantity.setText("Quantity :"+cart.getQuantity());

                int oneTypeProductPrice =((Integer.valueOf(cart.getPrice()))) * Integer.valueOf(cart.getQuantity());
                overTotalPrice=overTotalPrice+oneTypeProductPrice;
                textTotalAmount.setText("Total Price =$"+ String.valueOf(overTotalPrice));



                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        CharSequence options[] =new CharSequence[]
                                {
                                        "Edit" ,
                                        "Remove"

                                };
                        AlertDialog.Builder builder =new AlertDialog.Builder(Cartactivity.this);
                        builder.setTitle("Cart option:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i==0)
                                {
                                    Intent intent= new Intent(Cartactivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid",cart.getPid());
                                    startActivity(intent);
                                }
                                if (i==1)
                                {
                                    cartListRef.child("Users View")
                                            .child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(cart.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        Toast.makeText(Cartactivity.this,"Item removerd successfully.",Toast.LENGTH_SHORT).show();
                                                        Intent intent=new Intent(Cartactivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }

                                                }
                                            });
                                }

                            }


                        });
                        builder.show();

                    };


                });

            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();



    }

    private void checkOrderState()
    {
        DatabaseReference ordersRef;
        ordersRef= FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    String shipingState=snapshot.child("state").getValue().toString();
                    String userName =snapshot.child("name").getValue().toString();

                    if (shipingState.equals("shipped"))
                    {
                        textTotalAmount.setText("Dear "+userName+":\n Order is shipoped successfully.");
                        recyclerView.setVisibility(View.GONE);

                        tstMsg1.setVisibility(View.VISIBLE);
                        tstMsg1.setText("Congratulation ,your final order has been placed successfully. soon you will  received it at your home.");
                        NextProcessBtn.setVisibility(View.GONE);

                        Toast.makeText(Cartactivity.this,"you can purchase more products",Toast.LENGTH_SHORT).show();

                    }
                    else if (shipingState.equals("not shipped"))
                    {
                        textTotalAmount.setText("Dear "+userName+":\n shipping state =Not shipped.");
                        recyclerView.setVisibility(View.GONE);

                        tstMsg1.setVisibility(View.VISIBLE);
                        NextProcessBtn.setVisibility(View.GONE);

                        Toast.makeText(Cartactivity.this,"you can purchase more products",Toast.LENGTH_SHORT).show();


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}