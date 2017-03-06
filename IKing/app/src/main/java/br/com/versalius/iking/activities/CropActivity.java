package br.com.versalius.iking.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.edmodo.cropper.CropImageView;

import org.greenrobot.eventbus.EventBus;

import br.com.versalius.iking.R;


/**
 * Created by jn18 on 13/01/2017.
 */
public class CropActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        String imagePath = (String) getIntent().getExtras().get("imagePath");
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        final CropImageView cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        cropImageView.setImageBitmap(bitmap);

        ImageButton btCrop = (ImageButton) findViewById(R.id.btCrop);
        btCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = Bitmap.createScaledBitmap(cropImageView.getCroppedImage(),
//                        cropImageView.getCroppedImage().getWidth(),
                        200,
                        200,
                        true);
                EventBus.getDefault().post(bitmap);
                finish();
            }
        });
    }
}
