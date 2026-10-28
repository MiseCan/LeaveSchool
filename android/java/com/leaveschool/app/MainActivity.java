package com.leaveschool.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 用 WebView 承载 uni-app 的 H5 构建产物（assets/www）。
 *
 * 关键点：页面通过固定虚拟域名 http://app.local/ 加载，资源由 shouldInterceptRequest
 * 直接从 assets 返回。origin 固定，localStorage 里的请假记录才能跨启动保留。
 * （之前用 127.0.0.1 + 随机端口，每次启动端口不同 = 不同 origin，数据每次都丢。）
 */
public class MainActivity extends Activity {

    private static final String HOST = "app.local";
    private static final String START_URL = "http://" + HOST + "/index.html";
    private static final int REQUEST_FILE = 1001;

    private static final Map<String, String> MIME = new HashMap<>();

    static {
        MIME.put("html", "text/html");
        MIME.put("js", "application/javascript");
        MIME.put("css", "text/css");
        MIME.put("json", "application/json");
        MIME.put("png", "image/png");
        MIME.put("jpg", "image/jpeg");
        MIME.put("jpeg", "image/jpeg");
        MIME.put("gif", "image/gif");
        MIME.put("svg", "image/svg+xml");
        MIME.put("ico", "image/x-icon");
        MIME.put("ttf", "font/ttf");
        MIME.put("otf", "font/otf");
        MIME.put("woff", "font/woff");
        MIME.put("woff2", "font/woff2");
    }

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private boolean retried;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 参考界面是白底状态栏 + 深色图标
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0xFFFFFFFF);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFFFFFFFF);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        configureWebView();
        webView.loadUrl(START_URL);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG_WEBVIEW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return localAsset(request.getUrl());
            }

            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return localAsset(Uri.parse(url));
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://" + HOST)) {
                    return false;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                retried = false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    retryLater(view);
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                retryLater(view);
            }

            private void retryLater(final WebView view) {
                if (retried) {
                    return;
                }
                retried = true;
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.loadUrl(START_URL);
                    }
                }, 400);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }
                fileCallback = callback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*"});
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try {
                    startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_FILE);
                    return true;
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
            }
        });
    }

    /** 把 http://app.local/xxx 映射到 assets/www/xxx */
    private WebResourceResponse localAsset(Uri uri) {
        if (uri == null || !HOST.equals(uri.getHost())) {
            return null;
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = "/index.html";
        }
        InputStream stream = openAsset(path);
        if (stream == null) {
            path = "/index.html"; // SPA 回退
            stream = openAsset(path);
        }
        if (stream == null) {
            return null;
        }
        String mime = mimeOf(path);
        boolean text = mime.startsWith("text/") || mime.contains("javascript") || mime.contains("json");
        return new WebResourceResponse(mime, text ? "utf-8" : null, stream);
    }

    private InputStream openAsset(String path) {
        try {
            return getAssets().open("www" + path);
        } catch (IOException e) {
            return null;
        }
    }

    private String mimeOf(String path) {
        int dot = path.lastIndexOf('.');
        if (dot >= 0 && dot < path.length() - 1) {
            String value = MIME.get(path.substring(dot + 1).toLowerCase(Locale.US));
            if (value != null) {
                return value;
            }
        }
        return "application/octet-stream";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FILE) {
            if (fileCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            fileCallback.onReceiveValue(results);
            fileCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
        }
        super.onDestroy();
    }
}
