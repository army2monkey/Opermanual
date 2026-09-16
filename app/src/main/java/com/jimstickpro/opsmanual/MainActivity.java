package com.jimstickpro.opsmanual;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.print.PrintManager;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_IMPORT_BACKUP = 2001;
    private WebView webView;
    private boolean pageReady=false;
    private float insetTopDp=0, insetBottomDp=0;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w=getWindow(); w.setStatusBarColor(Color.rgb(63,14,23)); w.setNavigationBarColor(Color.WHITE);
        webView=new WebView(this); setContentView(webView);
        WebSettings s=webView.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true); s.setAllowFileAccessFromFileURLs(false); s.setAllowUniversalAccessFromFileURLs(false);
        webView.addJavascriptInterface(new NativeBridge(),"NativeAndroid");
        webView.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,String url){if(url!=null&&(url.startsWith("http://")||url.startsWith("https://"))){try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}catch(Exception e){Toast.makeText(MainActivity.this,"No browser available.",Toast.LENGTH_LONG).show();}return true;}return false;}});
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onJsAlert(WebView view,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("OK",(d,x)->result.confirm()).setOnCancelListener(d->result.cancel()).show();return true;}
            @Override public boolean onJsConfirm(WebView view,String url,String message,JsResult result){new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("OK",(d,x)->result.confirm()).setNegativeButton("Cancel",(d,x)->result.cancel()).setOnCancelListener(d->result.cancel()).show();return true;}
        });
        webView.setOnApplyWindowInsetsListener((v,insets)->{int top=0,bottom=0;if(android.os.Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());top=bars.top;bottom=bars.bottom;}else{top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();}float den=getResources().getDisplayMetrics().density;insetTopDp=top/den;insetBottomDp=bottom/den;pushInsets();return insets;});
        webView.loadUrl("file:///android_asset/index.html");
    }
    private void pushInsets(){if(!pageReady||webView==null)return;webView.evaluateJavascript("window.applyNativeInsets&&window.applyNativeInsets("+String.format(Locale.US,"%.1f",insetTopDp)+","+String.format(Locale.US,"%.1f",insetBottomDp)+")",null);}
    @Override public void onBackPressed(){if(webView==null){super.onBackPressed();return;}webView.evaluateJavascript("window.handleAndroidBack?window.handleAndroidBack():false",v->{if(!"true".equals(v)){if(webView.canGoBack())webView.goBack();else super.onBackPressed();}});}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode!=REQ_IMPORT_BACKUP||resultCode!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);String text=out.toString(StandardCharsets.UTF_8.name());String name=displayName(uri);String js="window.restoreOpsManualBackup("+JSONObject.quote(text)+","+JSONObject.quote(name)+")";webView.evaluateJavascript(js,null);}catch(Exception e){Toast.makeText(this,"Backup import failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    private String displayName(Uri uri){String name=null;try(Cursor c=getContentResolver().query(uri,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null)){if(c!=null&&c.moveToFirst()){int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(ix>=0)name=c.getString(ix);}}catch(Exception ignored){}return name==null?"backup.json":name;}
    private Uri writeDownload(String filename,String mime,InputStream source) throws Exception {ContentResolver r=getContentResolver();ContentValues v=new ContentValues();v.put(MediaStore.Downloads.DISPLAY_NAME,filename);v.put(MediaStore.Downloads.MIME_TYPE,mime);v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+"/Jims Ops Manual");Uri uri=r.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);if(uri==null)throw new IllegalStateException("Could not create download");try(OutputStream out=r.openOutputStream(uri)){byte[] b=new byte[8192];int n;while((n=source.read(b))!=-1)out.write(b,0,n);}return uri;}
    private Uri writeDownload(String filename,String mime,String text) throws Exception {return writeDownload(filename,mime,new java.io.ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));}
    public class NativeBridge {
        @JavascriptInterface public void pageReady(){runOnUiThread(()->{pageReady=true;pushInsets();});}
        @JavascriptInterface public void saveBackup(String json){runOnUiThread(()->{try{String d=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());writeDownload("Jims_Ops_Manual_Backup_"+d+".json","application/json",json);Toast.makeText(MainActivity.this,"Backup saved to Downloads / Jims Ops Manual",Toast.LENGTH_LONG).show();}catch(Exception e){Toast.makeText(MainActivity.this,"Backup failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}});}
        @JavascriptInterface public void pickBackup(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");startActivityForResult(i,REQ_IMPORT_BACKUP);});}
        @JavascriptInterface public void printPage(){runOnUiThread(()->{PrintManager pm=(PrintManager)getSystemService(PRINT_SERVICE);if(pm!=null)pm.print("Jim's Ops Manual",webView.createPrintDocumentAdapter("Jim's Ops Manual"),null);});}
        @JavascriptInterface public void shareText(String title,String text){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,title);i.putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(i,"Share section"));});}
        @JavascriptInterface public void saveReferencePdf(String which){runOnUiThread(()->{String asset="quick".equals(which)?"reference_quick_guide.pdf":"reference_full_manual.pdf";String name="quick".equals(which)?"Jims_Tick_Mosquito_Quick_Operations_Guide.pdf":"Jims_Tick_Mosquito_Operations_Growth_Expansion_Manual_2027.pdf";try(InputStream in=getAssets().open(asset)){writeDownload(name,"application/pdf",in);Toast.makeText(MainActivity.this,"PDF saved to Downloads / Jims Ops Manual",Toast.LENGTH_LONG).show();}catch(Exception e){Toast.makeText(MainActivity.this,"PDF save failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}});}
    }
}
