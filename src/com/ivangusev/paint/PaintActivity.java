package com.ivangusev.paint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class PaintActivity extends Activity implements View.OnClickListener {

    public static final String FILE_PATH = "file_path";

    private PainterView painterView;

    private ToggleButton arrowToggle;
    private ToggleButton circleToggle;
    private ToggleButton rectToggle;
    private ToggleButton textToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        painterView = (PainterView) findViewById(R.id.canvas);

        arrowToggle = (ToggleButton) findViewById(R.id.radio_arrow);
        circleToggle = (ToggleButton) findViewById(R.id.radio_circle);
        rectToggle = (ToggleButton) findViewById(R.id.radio_rect);
        textToggle = (ToggleButton) findViewById(R.id.font_text);

        setClickListener(new int[]{R.id.saveButton, R.id.close, R.id.radio_arrow, R.id.radio_circle,
                R.id.radio_rect, R.id.font_text, R.id.back, R.id.clear, R.id.changeColor});

        arrowToggle.setChecked(true);
        painterView.setBitmapSrc(getIntent().getStringExtra(FILE_PATH));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setClickListener(int[] ids) {
        for (int id : ids) {
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveButton:
                setResult(RESULT_OK, new Intent().putExtra(FILE_PATH, painterView.saveImage()));
                finish();
                break;
            case R.id.close:
                finish();
                break;
            case R.id.back:
                painterView.back();
                break;
            case R.id.clear:
                painterView.clear();
                break;
            case R.id.changeColor:
                new ColorPickerDialog(this,
                        new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                painterView.setPaintColor(color);
                            }
                        }, painterView.getPaintColor()).show();
                break;
            case R.id.radio_arrow:
                changeView((ToggleButton) view, PainterView.MODE_ARROW);
                break;
            case R.id.radio_circle:
                changeView((ToggleButton) view, PainterView.MODE_CIRCLE);
                break;
            case R.id.radio_rect:
                changeView((ToggleButton) view, PainterView.MODE_RECT);
                break;
            case R.id.font_text:
                changeView((ToggleButton) view, PainterView.MODE_TEXT);
                break;
        }
    }

    private void changeView(ToggleButton button, int mode) {
        painterView.switchMode(mode);
        arrowToggle.setChecked(false);
        circleToggle.setChecked(false);
        rectToggle.setChecked(false);
        textToggle.setChecked(false);
        button.setChecked(true);
    }
}
