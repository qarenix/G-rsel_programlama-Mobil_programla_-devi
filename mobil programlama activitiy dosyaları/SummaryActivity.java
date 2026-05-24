package com.ensoz.varlikyonetimi;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class SummaryActivity extends Activity {
    private static final int BRAND = Color.rgb(20, 103, 94);
    private static final int BRAND_DARK = Color.rgb(8, 68, 62);
    private static final int SCREEN = Color.rgb(246, 248, 247);
    private static final int TEXT = Color.rgb(31, 42, 48);
    private static final int MUTED = Color.rgb(103, 116, 124);

    private List<Asset> assets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assets = new AssetStore(this).load();
        setContentView(buildContent());
    }

    private View buildContent() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(SCREEN);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(16), dp(16), dp(16));
        scrollView.addView(page);

        LinearLayout header = panel(BRAND, dp(18), dp(18));
        header.setBackground(gradient(BRAND_DARK, BRAND, dp(18)));
        page.addView(header, new LinearLayout.LayoutParams(-1, -2));

        TextView title = text("Toplam Varlıklar", 24, Color.WHITE, Typeface.BOLD);
        header.addView(title);

        TextView subtitle = text("Eklenen bütün varlıkların özet ekranı", 14, Color.argb(225, 255, 255, 255), Typeface.NORMAL);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        header.addView(subtitle);

        TextView total = text("Kayıt Sayısı: " + assets.size() + "\nToplam Değer: " + money(totalAssetValue()), 17, Color.WHITE, Typeface.BOLD);
        total.setPadding(dp(14), dp(12), dp(14), dp(12));
        total.setBackground(card(Color.argb(40, 255, 255, 255), dp(12), Color.argb(70, 255, 255, 255)));
        header.addView(total);

        Button back = new Button(this);
        back.setText("Geri Dön");
        back.setAllCaps(false);
        back.setTextColor(BRAND_DARK);
        back.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        back.setBackground(card(Color.WHITE, dp(14), 0));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(-1, dp(48));
        backParams.setMargins(0, dp(12), 0, dp(12));
        page.addView(back, backParams);

        if (assets.isEmpty()) {
            TextView empty = text("Henüz varlık eklenmedi.", 16, MUTED, Typeface.BOLD);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(28), 0, dp(28));
            page.addView(empty);
            return scrollView;
        }

        for (Asset asset : assets) {
            page.addView(assetCard(asset));
        }

        return scrollView;
    }

    private View assetCard(Asset asset) {
        LinearLayout cardView = panel(Color.WHITE, dp(16), dp(14));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(10));
        cardView.setLayoutParams(params);

        TextView name = text(asset.name, 18, TEXT, Typeface.BOLD);
        cardView.addView(name);

        TextView category = text(asset.category + " - " + asset.status + " - " + money(asset.value), 14, BRAND_DARK, Typeface.BOLD);
        category.setPadding(0, dp(4), 0, dp(8));
        cardView.addView(category);

        TextView details = text(
                "Konum: " + safe(asset.location) +
                        "\nSorumlu: " + safe(asset.owner) +
                        "\nBilgi: " + detailSummary(asset),
                13,
                MUTED,
                Typeface.NORMAL
        );
        cardView.addView(details);
        return cardView;
    }

    private String detailSummary(Asset asset) {
        StringBuilder builder = new StringBuilder();
        append(builder, asset.detail1);
        append(builder, asset.detail2);
        append(builder, asset.detail3);
        append(builder, asset.detail4);
        return builder.length() == 0 ? "Yok" : builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (safe(value).trim().isEmpty()) return;
        if (builder.length() > 0) builder.append(" | ");
        builder.append(value.trim());
    }

    private double totalAssetValue() {
        double total = 0;
        for (Asset asset : assets) {
            total += asset.value;
        }
        return total;
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(safe(value));
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        return view;
    }

    private LinearLayout panel(int color, int radius, int padding) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackground(card(color, radius, color == Color.WHITE ? Color.rgb(226, 232, 230) : 0));
        layout.setElevation(dp(2));
        return layout;
    }

    private GradientDrawable card(int fill, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (stroke != 0) {
            drawable.setStroke(dp(1), stroke);
        }
        return drawable;
    }

    private GradientDrawable gradient(int start, int end, int radius) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end});
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private String money(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("tr", "TR"));
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,###", symbols);
        return format.format(Math.max(0, value)) + " TL";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
