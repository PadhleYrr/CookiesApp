package com.cookiegrabber.app;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private ProgressBar progressBar;

    private static final String HOME_URL = "https://www.google.com";

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView    = findViewById(R.id.webview);
        urlInput   = findViewById(R.id.url_input);
        progressBar = findViewById(R.id.progress_bar);

        ImageButton btnBack    = findViewById(R.id.btn_back);
        ImageButton btnForward = findViewById(R.id.btn_forward);
        ImageButton btnReload  = findViewById(R.id.btn_reload);
        MaterialButton btnCookies = findViewById(R.id.btn_cookies);
        MaterialButton btnHome    = findViewById(R.id.btn_home);

        // ── WebView settings ──
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/124.0.0.0 Mobile Safari/537.36"
        );

        // ── Cookie settings ──
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // ── WebViewClient: update URL bar, handle navigation ──
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                urlInput.setText(url);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                urlInput.setText(url);
                progressBar.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }
        });

        // ── Progress bar ──
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                // optionally set activity title
            }
        });

        // ── URL bar: Go button / Enter key ──
        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                navigateTo(urlInput.getText().toString().trim());
                return true;
            }
            return false;
        });

        // ── Nav buttons ──
        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });

        btnForward.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });

        btnReload.setOnClickListener(v -> webView.reload());

        btnHome.setOnClickListener(v -> navigateTo(HOME_URL));

        // ── Cookie button ──
        btnCookies.setOnClickListener(v -> showCookieSheet());

        // ── Load home ──
        navigateTo(HOME_URL);
    }

    // ─────────────────────────────────────────
    //  Navigate
    // ─────────────────────────────────────────
    private void navigateTo(String raw) {
        if (raw == null || raw.isEmpty()) return;

        String url;
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            url = raw;
        } else if (raw.contains(".") && !raw.contains(" ")) {
            // Looks like a domain
            url = "https://" + raw;
        } else {
            // Search query
            url = "https://www.google.com/search?q=" + android.net.Uri.encode(raw);
        }

        webView.loadUrl(url);
        hideKeyboard();
    }

    // ─────────────────────────────────────────
    //  Cookie bottom sheet
    // ─────────────────────────────────────────
    private void showCookieSheet() {
        String currentUrl = webView.getUrl();
        if (currentUrl == null || currentUrl.isEmpty()) {
            toast("No page loaded");
            return;
        }

        // Get cookies via Android CookieManager (works cross-origin, includes HttpOnly)
        CookieManager cm = CookieManager.getInstance();
        String rawCookies = cm.getCookie(currentUrl);

        List<String[]> cookiePairs = new ArrayList<>();
        if (rawCookies != null && !rawCookies.isEmpty()) {
            String[] parts = rawCookies.split(";");
            for (String part : parts) {
                part = part.trim();
                int eq = part.indexOf('=');
                if (eq > 0) {
                    String name  = part.substring(0, eq).trim();
                    String value = part.substring(eq + 1).trim();
                    cookiePairs.add(new String[]{name, value});
                } else if (!part.isEmpty()) {
                    cookiePairs.add(new String[]{part, ""});
                }
            }
        }

        // Build bottom sheet
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetTheme);
        View sheet = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_cookies, null);
        dialog.setContentView(sheet);

        // Domain label
        TextView tvDomain = sheet.findViewById(R.id.tv_cookie_domain);
        try {
            tvDomain.setText(new java.net.URL(currentUrl).getHost());
        } catch (Exception e) {
            tvDomain.setText(currentUrl);
        }

        // Populate cookie list
        LinearLayout container = sheet.findViewById(R.id.cookie_list_container);
        final List<String[]> finalPairs = cookiePairs;

        if (finalPairs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No cookies found for this page");
            empty.setTextColor(Color.parseColor("#555555"));
            empty.setPadding(0, 30, 0, 30);
            empty.setGravity(android.view.Gravity.CENTER);
            container.addView(empty);
        } else {
            for (String[] pair : finalPairs) {
                View item = LayoutInflater.from(this).inflate(R.layout.item_cookie, container, false);
                ((TextView) item.findViewById(R.id.tv_cookie_name)).setText(pair[0]);
                ((TextView) item.findViewById(R.id.tv_cookie_value)).setText(
                    pair[1].isEmpty() ? "(empty)" : pair[1]
                );
                item.setOnClickListener(v -> {
                    copyToClipboard(pair[0] + "=" + pair[1]);
                    toast(pair[0] + " copied!");
                });
                container.addView(item);
            }
        }

        // Copy All button
        sheet.findViewById(R.id.btn_copy_all).setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (String[] pair : finalPairs) {
                if (sb.length() > 0) sb.append("; ");
                sb.append(pair[0]).append("=").append(pair[1]);
            }
            copyToClipboard(sb.toString());
            toast(finalPairs.size() + " cookies copied!");
            dialog.dismiss();
        });

        // Copy JSON button
        sheet.findViewById(R.id.btn_copy_json).setOnClickListener(v -> {
            try {
                JSONObject json = new JSONObject();
                for (String[] pair : finalPairs) {
                    json.put(pair[0], pair[1]);
                }
                copyToClipboard(json.toString(2));
                toast("JSON copied!");
                dialog.dismiss();
            } catch (Exception e) {
                toast("JSON error: " + e.getMessage());
            }
        });

        // Close button
        sheet.findViewById(R.id.btn_close_sheet).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // ─────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────
    private void copyToClipboard(String text) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("cookies", text));
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
    }

    // ── Handle back button for WebView history ──
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
