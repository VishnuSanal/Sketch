package phone.vishnu.sketchit.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

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

    private ImageView strokeWidthIV;
    private SketchView sketchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        sketchView = findViewById(R.id.view);
        strokeWidthIV = findViewById(R.id.strokeWidthIV);

        strokeWidthIV.setPadding((45 - 5) / 2, (45 - 5) / 2, (45 - 5) / 2, (45 - 5) / 2);

        findViewById(R.id.arrowIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (strokeWidthIV.getAlpha() == 0f) {
                    findViewById(R.id.arrowIV).animate().rotationBy(180);

                    strokeWidthIV.animate().translationXBy(-120 * 4).alpha(1f);
                    findViewById(R.id.colorChooseIV).animate().translationXBy(-120 * 3).alpha(1f);
                    findViewById(R.id.saveIV).animate().translationXBy(-120 * 2).alpha(1f);
                    findViewById(R.id.clearAllIV).animate().translationXBy(-120).alpha(1f);
                } else {
                    findViewById(R.id.arrowIV).animate().rotationBy(180);

                    strokeWidthIV.animate().translationXBy(120 * 4).alpha(0f);
                    findViewById(R.id.colorChooseIV).animate().translationXBy(120 * 3).alpha(0f);
                    findViewById(R.id.saveIV).animate().translationXBy(120 * 2).alpha(0f);
                    findViewById(R.id.clearAllIV).animate().translationXBy(120).alpha(0f);
                }
            }
        });


        findViewById(R.id.colorChooseIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    ColorPickerDialogBuilder
                            .with(MainActivity.this)
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
            }
        });

        strokeWidthIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    int width = sketchView.getLineWidth();

                    if (width >= 45) {
                        sketchView.setLineWidth(width = 5);
                        strokeWidthIV.setPadding((45 - width) / 2, (45 - width) / 2, (45 - width) / 2, (45 - width) / 2);
                    } else {
                        sketchView.setLineWidth(width += 10);
                        strokeWidthIV.setPadding(width, width, width, width);
                        strokeWidthIV.setPadding((45 - width) / 2, (45 - width) / 2, (45 - width) / 2, (45 - width) / 2);
                    }
                }
            }
        });

        findViewById(R.id.clearAllIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchView.clear();
            }
        });

        findViewById(R.id.saveIV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "", "Please Wait");

                    if (!isPermissionGranted())
                        isPermissionGranted();
                    else {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {

                                if (!sketchView.isNull()) {

                                    File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "SketchIt");
                                    if (!root.exists()) //noinspection ResultOfMethodCallIgnored
                                        root.mkdirs();

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

                                        MediaScannerConnection.scanFile(MainActivity.this, new String[]{file}, null, null);

                                        progressDialog.dismiss();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        progressDialog.dismiss();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 22) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showPermissionDeniedDialog();
                } else {
                    int PERMISSION_REQ_CODE = 2;
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private void showPermissionDeniedDialog() {

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("Please Accept Permission");
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                int PERMISSION_REQ_CODE = 2;
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQ_CODE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }
}

