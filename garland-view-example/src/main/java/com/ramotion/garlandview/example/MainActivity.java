package com.ramotion.garlandview.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ramotion.garlandview.TailItemTransformer;
import com.ramotion.garlandview.TailLayoutManager;
import com.ramotion.garlandview.TailRecyclerView;
import com.ramotion.garlandview.TailSnapHelper;
import com.ramotion.garlandview.example.outer.OuterAdapter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecyclerView();
    }

    private void initRecyclerView() {
        final TailRecyclerView rv = (TailRecyclerView) findViewById(R.id.recycler_view);
        rv.setLayoutManager(new TailLayoutManager(this).setPageTransformer(new TailItemTransformer()));
        rv.setAdapter(new OuterAdapter());

        new TailSnapHelper().attachToRecyclerView(rv);
    }

}
