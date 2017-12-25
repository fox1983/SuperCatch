package me.chunsheng.supercatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;
import me.chunsheng.smilecatch.CrashHandler;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);
    }


    /**
     * 重启APP
     *
     * @param view
     */
    public void restartApp(View view) {
        try {
            //这里跳转Activity可以通过类名，动态设置，而不是写死Launcher Activity
            Intent intent = new Intent(this, Class.forName("me.chunsheng.supercatch.MainActivity"));
            intent.putExtra("crash", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上报crash,使用自定义CodeView展示日志
     *
     * @param view
     */
    public void reportCrash(final View view) {
        String crashText = CrashHandler.getInstance(this).getCrash();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.crash_dialog, null);
        builder.setView(dialogView);
        final CodeView mCodeView = (CodeView) dialogView.findViewById(R.id.codeView);
        mCodeView
                .setTheme(Theme.AGATE)
                .setCode(crashText)
                .setLanguage(Language.JAVA)
                .setWrapLine(true)
                .setFontSize(14)
                .setZoomEnabled(false)
                .setShowLineNumber(false)
                .setStartLineNumber(9000)
                .apply();
        builder.setTitle("Crash日志").
                setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        restartApp(view);
                    }
                }).setNegativeButton("上报", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(CrashActivity.this, "假装上报成功", Toast.LENGTH_SHORT).show();

                new Timer().schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                restartApp(view);
                            }
                        },
                        2000
                );
            }
        });
        builder.create().show();
    }

    /**
     * 上报crash，使用系统自带WebView展示日志
     *
     * @param view
     */
    public void reportCrashBak(final View view) {

        String crashText = CrashHandler.getInstance(this).getCrash();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.crash_dialog, null);
        builder.setView(dialogView);
        final WebView webview = (WebView) dialogView.findViewById(R.id.webview);
        WebSettings settings = webview.getSettings();
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        //to remove padding and margin
        webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        String formatText = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head> \n" +
                "<meta charset=\"utf-8\">  \n" +
                "<body>\n" +
                "<link href=\"http://cdn.bootcss.com/highlight.js/8.0/styles/monokai_sublime.min.css\" rel=\"stylesheet\">  \n" +
                "<script src=\"http://cdn.bootcss.com/highlight.js/8.0/highlight.min.js\"></script>  \n" +
                "<script >hljs.initHighlightingOnLoad();</script>  \n" +
                "</head>\n" +
                "<pre><code class=\"lang-javascript\">\n" +
                crashText.replace("\n", "<br/>") +
                "</code></pre>\n" +
                "</body>\n" +
                "</html>";

        webview.loadData(formatText, "text/html", "UTF-8");
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                restartApp(view);
            }
        }).setNegativeButton("上报", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(CrashActivity.this, "假装上报成功", Toast.LENGTH_SHORT).show();
            }
        });
        builder.create().show();

    }


}
