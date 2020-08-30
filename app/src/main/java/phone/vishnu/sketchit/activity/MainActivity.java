package phone.vishnu.sketchit.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import phone.vishnu.sketchit.R;
import phone.vishnu.sketchit.view.SketchView;

public class MainActivity extends AppCompatActivity {

    private SketchView sketchView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView imageView;
    private AlertDialog widthAlertDialog;
    private ImageView colorChooser, strokeWidth, clearAll, saveAll;
    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            Paint p = new Paint();
            p.setColor(sketchView.getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, p);
            imageView.setImageBitmap(bitmap);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        sketchView = findViewById(R.id.view);

        colorChooser = findViewById(R.id.colorChooseIV);
        strokeWidth = findViewById(R.id.strokeWidthIV);
        clearAll = findViewById(R.id.clearAllIV);
        saveAll = findViewById(R.id.saveIV);

        colorChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorAlertDialog();
            }
        });

        strokeWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWidthAlertDialog();
            }
        });

        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchView.clear();
            }
        });

        saveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

    }

    void showColorAlertDialog() {
        ColorPickerDialogBuilder
                .with(this)
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(11)
                .showLightnessSlider(false)
//                .showAlphaSlider(false)
                .setPositiveButton("O.K", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        sketchView.setDrawingColor(lastSelectedColor);
                        d.dismiss();
                    }
                })
                .build()
                .show();
    }

    void showWidthAlertDialog() {

        currentAlertDialog = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.set_width_dialog, null);
        final SeekBar seekBar = view.findViewById(R.id.widthSeekBar);
        Button button = view.findViewById(R.id.widthButton);
        imageView = view.findViewById(R.id.widthImageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchView.setLineWidth(seekBar.getProgress());
                widthAlertDialog.dismiss();
                currentAlertDialog = null;
            }

        });

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBar.setProgress(sketchView.getLineWidth());

        currentAlertDialog.setView(view);
        widthAlertDialog = currentAlertDialog.create();
        widthAlertDialog.show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 22) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(MainActivity.this, "Please Accept Required Permission", Toast.LENGTH_SHORT).show();
                }
                int PERMISSION_REQ_CODE = 222;
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
            } else {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        if (!sketchView.isNull()) {

                            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "SketchIt");
                            if (!root.exists()) root.mkdirs();

                            SharedPreferences sharedPreferences = getSharedPreferences("phone.vishnu.statussaver", Context.MODE_PRIVATE);
                            int lastInt = (sharedPreferences.getInt("number", 0)) + 1;
                            String file = root.toString() + File.separator + "SketchIt" + lastInt + ".jpg";
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("number", lastInt);
                            editor.apply();

                            Bitmap result = Bitmap.createBitmap(sketchView.getWidth(), sketchView.getHeight(), Bitmap.Config.ARGB_8888);
                            Canvas c = new Canvas(result);
                            sketchView.draw(c);

                            try {
                                FileOutputStream fOutputStream = new FileOutputStream(file);
                                final BufferedOutputStream bos = new BufferedOutputStream(fOutputStream);

                                result.compress(Bitmap.CompressFormat.JPEG, 90, bos);

                                fOutputStream.flush();
                                fOutputStream.close();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            MediaScannerConnection.scanFile(MainActivity.this, new String[]{file}, null, null);

                        }
                    }
                });
            }
        }
    }
}

