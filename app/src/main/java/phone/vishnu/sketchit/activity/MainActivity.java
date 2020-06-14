package phone.vishnu.sketchit.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import phone.vishnu.sketchit.R;
import phone.vishnu.sketchit.view.SketchView;

public class MainActivity extends AppCompatActivity {
    private SketchView sketchView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView imageView;
    private AlertDialog widthAlertDialog, colorAlertDialog;
    private SeekBar redSeekBar, greenSeekBar, alphaSeekBar, blueSeekBar;
    private View colorView;

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
    private SeekBar.OnSeekBarChangeListener colorSeekBarChangedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            colorView.setBackgroundColor(Color.argb(alphaSeekBar.getProgress(), redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));

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

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.notification_clear_all) {
            sketchView.clear();
        } else if (item.getItemId() == R.id.notification_line_width) {
            showWidthAlertDialog();
        } else if (item.getItemId() == R.id.notification_pen_color) {
            showColorAlertDialog();
        } else if (item.getItemId() == R.id.notification_save) {
            requestPermission();
        }
        return super.onOptionsItemSelected(item);
    }*/

    void showColorAlertDialog() {
        currentAlertDialog = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.color_dialog, null);

        redSeekBar = view.findViewById(R.id.redSeekBar);
        greenSeekBar = view.findViewById(R.id.greenSeekBar);
        blueSeekBar = view.findViewById(R.id.blueSeekBar);
        alphaSeekBar = view.findViewById(R.id.alphaSeekBar);

        colorView = view.findViewById(R.id.colorView);
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChangedListener);
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChangedListener);
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChangedListener);
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChangedListener);

        int color = sketchView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        Button button = view.findViewById(R.id.setColorButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sketchView.setDrawingColor(Color.argb(alphaSeekBar.getProgress(), redSeekBar.getProgress(), greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                colorAlertDialog.dismiss();
            }
        });

        currentAlertDialog.setView(view);
        colorAlertDialog = currentAlertDialog.create();
        colorAlertDialog.show();

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
                        sketchView.generateNoteOnSD();
                    }
                });

            }
        }
    }
}
