package com.bahri.whatsappclone.view.activities.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.bahri.whatsappclone.R;
import com.bahri.whatsappclone.common.Common;
import com.bahri.whatsappclone.databinding.ActivityViewImageBinding;

public class ViewImage extends AppCompatActivity {

    private ActivityViewImageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_view_image);

        binding.imageView.setImageBitmap(Common.IMAGE_BITMAP);

    }
}
