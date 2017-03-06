package br.com.versalius.iking.utils;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.versalius.iking.R;


/**
 * Created by jn18 on 13/01/2017.
 */
public class CustomSnackBar {
    private static Snackbar snackbar;

    public static Snackbar make(ViewGroup view, String text, int duration, int type) {
        snackbar = Snackbar.make(view, text, duration);
        TextView snackTextView = (TextView) snackbar
                .getView()
                .findViewById(android.support.design.R.id.snackbar_text);
        snackTextView.setTextColor(Color.rgb(255, 255, 255));

        switch (type){
            case SnackBarType.ERROR:
                snackbar.getView().setBackgroundResource(R.color.snackBackgroundError);
                break;
            case SnackBarType.SUCCESS:
                snackbar.getView().setBackgroundResource(R.color.snackBackgroundSuccess);
                break;
            case SnackBarType.INFO:
                snackbar.getView().setBackgroundResource(R.color.snackBackgroundInfo);
                break;
        }

        return snackbar;
    }

    public class SnackBarType {
        public static final int ERROR = 0;
        public static final int SUCCESS = 1;
        public static final int INFO = 3;
    }
}
